/**
 * Cloudflare Pages Function: /api/[[path]]
 * Edge Proxy and Fallback Router.
 *
 * If API_BACKEND_URL is configured in Cloudflare Pages environment variables,
 * this function transparently proxies API traffic to your live backend.
 */
export async function onRequest(context) {
  const { request, env, params } = context;
  const backendUrl = env.API_BACKEND_URL;

  // Handle CORS preflight requests
  if (request.method === "OPTIONS") {
    return new Response(null, {
      status: 204,
      headers: {
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Methods": "GET, POST, PUT, PATCH, DELETE, OPTIONS",
        "Access-Control-Allow-Headers": "Content-Type, Authorization, X-Requested-With",
        "Access-Control-Max-Age": "86400"
      }
    });
  }

  // If backend URL is provided, proxy the request to the Spring Boot cluster
  if (backendUrl) {
    try {
      const url = new URL(request.url);
      const cleanBackend = backendUrl.replace(/\/$/, "");
      const targetUrl = `${cleanBackend}${url.pathname}${url.search}`;

      const headers = new Headers(request.headers);
      headers.set("X-Forwarded-Host", url.hostname);
      headers.set("X-Forwarded-Proto", url.protocol.replace(":", ""));

      const proxyRequest = new Request(targetUrl, {
        method: request.method,
        headers: headers,
        body: ["GET", "HEAD"].includes(request.method) ? undefined : request.body,
        redirect: "follow"
      });

      const response = await fetch(proxyRequest);
      const newHeaders = new Headers(response.headers);
      newHeaders.set("Access-Control-Allow-Origin", "*");

      return new Response(response.body, {
        status: response.status,
        statusText: response.statusText,
        headers: newHeaders
      });
    } catch (err) {
      return new Response(JSON.stringify({
        error: "Backend proxy error",
        message: err.message,
        timestamp: new Date().toISOString()
      }), {
        status: 502,
        headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" }
      });
    }
  }

  // Standalone edge response when no external backend is bound
  const path = Array.isArray(params.path) ? params.path.join("/") : (params.path || "");
  return new Response(JSON.stringify({
    status: "ok",
    edge: "Cloudflare Pages Edge",
    endpoint: `/api/${path}`,
    note: "Running in standalone Cloudflare edge mode. To link to your live Spring Boot API cluster, define API_BACKEND_URL in Cloudflare Pages Dashboard > Settings > Environment Variables.",
    timestamp: new Date().toISOString()
  }), {
    status: 200,
    headers: {
      "Content-Type": "application/json",
      "Access-Control-Allow-Origin": "*"
    }
  });
}
