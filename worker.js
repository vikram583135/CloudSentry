/**
 * CloudSentry — Cloudflare Worker Entry Point
 * Serves the embedded SRE Web Console via Cloudflare Worker Assets
 * and proxies API requests to your Spring Boot cluster when configured.
 */

export default {
  async fetch(request, env) {
    const url = new URL(request.url);

    // 1. Edge Health Check Endpoint
    if (url.pathname === "/api/health" || url.pathname === "/api/v1/health") {
      const colo = request.cf ? request.cf.colo : "edge";
      const country = request.cf ? request.cf.country : "global";

      return new Response(JSON.stringify({
        status: "UP",
        platform: "Cloudflare Worker Edge",
        timestamp: new Date().toISOString(),
        edgeLocation: `${colo} (${country})`,
        version: "1.0.0"
      }), {
        status: 200,
        headers: {
          "Content-Type": "application/json",
          "Access-Control-Allow-Origin": "*",
          "Cache-Control": "no-cache"
        }
      });
    }

    // 2. CORS Preflight Handling for API calls
    if (request.method === "OPTIONS" && url.pathname.startsWith("/api/")) {
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

    // 3. Proxy API requests to live Spring Boot backend if API_BACKEND_URL is set
    if (url.pathname.startsWith("/api/") && env.API_BACKEND_URL) {
      try {
        const cleanBackend = env.API_BACKEND_URL.replace(/\/$/, "");
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
          headers: {
            "Content-Type": "application/json",
            "Access-Control-Allow-Origin": "*"
          }
        });
      }
    }

    // 4. Serve Static Assets via Cloudflare Assets Binding
    if (env.ASSETS) {
      return env.ASSETS.fetch(request);
    }

    return new Response("CloudSentry Platform Worker Active", {
      status: 200,
      headers: { "Content-Type": "text/plain" }
    });
  }
};
