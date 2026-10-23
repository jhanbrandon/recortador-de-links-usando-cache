import { useEffect, useRef, useState } from 'react';
import { linkApi } from './services/linkApi';
import CopyButton from './components/CopyButton';
import CacheTimer from './components/CacheTimer';
export default function App() {
 const [module, setModule] = useState(() => window.location.hash === '#enlaces' ? 'list' : 'create');
 const [url, setUrl] = useState('');
 const [result, setResult] = useState(null);
 const [links, setLinks] = useState([]);
 const [cache, setCache] = useState(null);
 const [busy, setBusy] = useState(false);
 const [error, setError] = useState('');
 const version = useRef(0);
 async function load() {
  const current = ++version.current;
  setBusy(true); setError('');
  const started = Date.now();
  try {
   const data = await linkApi.list();
   if(current !== version.current) return;
   setLinks(data.links);
   // Redis is the source of truth; request start conservatively accounts for network latency.
   setCache({ ...data.cache, deadline: started + data.cache.ttlSeconds * 1000 });
  } catch(e) { if(current === version.current) setError(e.message); }
  finally { if(current === version.current) setBusy(false); }
 }
 useEffect(() => {
  window.location.hash = module === 'list' ? 'enlaces' : 'acortar';
  if(module === 'list') load();
  else { version.current++; setBusy(false); setError(''); }
 }, [module]);
 // Resynchronize on tab focus, using status only: this does not rebuild the cache.
 useEffect(() => {
  if(module !== 'list') return;
  async function sync() {
   if(document.visibilityState !== 'visible') return;
   const started = Date.now();
   try { const status = await linkApi.status(); setCache(old => old ? { ...old, ...status, deadline: started + status.ttlSeconds * 1000 } : old); }
   catch { /* A subsequent explicit list request reports connection errors. */ }
  }
  document.addEventListener('visibilitychange', sync);
  return () => document.removeEventListener('visibilitychange', sync);
 }, [module]);
 async function shorten(event) {
  event.preventDefault(); setBusy(true); setError(''); setResult(null);
  try { setResult(await linkApi.create(url)); setCache(null); }
  catch(e) { setError(e.message); }
  finally { setBusy(false); }
 }
 return <div className="app">
  <header className="masthead"><a className="brand" href="#acortar" onClick={() => setModule('create')}><span className="brand-icon">↗</span> ENLACE<span className="brand-dot">.</span></a><span className="course">Programación Avanzada <span> / </span> Parcial 2</span></header>
  <main><div className="intro"><p className="eyebrow">MENOS CARACTERES. EL MISMO DESTINO.</p><h1>Acortador de enlaces</h1><p>Convierte una dirección extensa en un enlace fácil de compartir.</p></div>
   <nav aria-label="Módulos"><button className={module === 'create' ? 'tab active' : 'tab'} onClick={() => setModule('create')}>↗ <span>Acortar enlace</span></button><button className={module === 'list' ? 'tab active' : 'tab'} onClick={() => setModule('list')}>☷ <span>Ver enlaces</span></button></nav>
   <section className="panel"><div className="panel-heading"><div><p className="eyebrow">{module === 'create' ? '01 / CREAR' : '02 / CONSULTAR'}</p><h2>{module === 'create' ? 'Un enlace más simple' : 'Tus enlaces guardados'}</h2></div>{module === 'list' && cache && <CacheTimer cache={cache}/>}</div>
    {error && <p className="error" role="alert">{error}</p>}
    {module === 'create' ? <><form onSubmit={shorten}><label htmlFor="url">Enlace original</label><p className="hint">Pega una dirección completa que comience por http:// o https://</p><div className="input-row"><input id="url" type="url" required value={url} onChange={e => setUrl(e.target.value)} placeholder="https://ejemplo.com/una-direccion-muy-larga" disabled={busy}/><button className="primary" disabled={busy}>{busy ? 'Acortando…' : 'Acortar →'}</button></div></form>
     {result ? <div className="result" role="status"><p className="eyebrow">TU ENLACE ESTÁ LISTO</p><div className="result-row"><a href={result.shortUrl} target="_blank" rel="noreferrer">{result.shortUrl}</a><CopyButton url={result.shortUrl}/></div><p className="original">{result.originalUrl}</p></div> : <div className="empty-preview"><span>↗</span><p>Tu próximo enlace corto aparecerá aquí.</p></div>}
    </> : <><div className="table-toolbar"><p>{links.length} {links.length === 1 ? 'enlace registrado' : 'enlaces registrados'}{cache && ` · Última consulta: ${cache.source === 'redis' ? 'Redis' : 'MongoDB'}`}</p><button className="secondary" onClick={load} disabled={busy}>{busy ? 'Consultando…' : 'Actualizar listado'}</button></div><div className="table-wrap"><table><thead><tr><th>ID</th><th>Enlace original</th><th>Enlace acortado</th><th>Acción</th></tr></thead><tbody>{links.map(link => <tr key={link.id}><td className="id" title={link.id}>{link.id}</td><td className="original-cell"><a href={link.originalUrl} target="_blank" rel="noreferrer">{link.originalUrl}</a></td><td><a href={link.shortUrl} target="_blank" rel="noreferrer">{link.shortUrl}</a></td><td><CopyButton url={link.shortUrl}/></td></tr>)}</tbody></table></div>{links.length === 0 && !busy && <div className="empty-preview"><span>☷</span><p>Aún no hay enlaces. Crea el primero en «Acortar enlace».</p></div>}</>}
   </section><div className="storage-note"><span>●</span> Enlaces permanentes en MongoDB <b>·</b> Caché de consultas en Redis durante 30 segundos</div>
  </main><footer><span>Un proyecto de Programación Avanzada</span><span>React + Spring Boot</span></footer>
 </div>;
}