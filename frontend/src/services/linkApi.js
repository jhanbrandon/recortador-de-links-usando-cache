const BASE = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '');
async function request(path, options) {
 let response;
 try { response = await fetch(`${BASE}${path}`, { cache: 'no-store', ...options }); }
 catch { throw new Error('No se pudo conectar con el backend. Comprueba que Spring Boot esté iniciado.'); }
 const data = await response.json().catch(() => ({}));
 if (!response.ok) throw new Error(data.message || `La solicitud falló (${response.status}).`);
 return data;
}
export const linkApi = {
 create: originalUrl => request('/api/links', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ originalUrl }) }),
 list: () => request('/api/links'),
 status: () => request('/api/links/cache/status'),
};