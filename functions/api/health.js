/**
 * Cloudflare Pages Function: /api/health
 * Edge health check endpoint returning edge data center and status.
 */
export async function onRequest(context) {
  const colo = context.request.cf ? context.request.cf.colo : "global";
  const country = context.request.cf ? context.request.cf.country : "unknown";

  return new Response(JSON.stringify({
    status: "UP",
    platform: "Cloudflare Pages Edge",
    timestamp: new Date().toISOString(),
    edgeColo: colo,
    edgeCountry: country,
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
