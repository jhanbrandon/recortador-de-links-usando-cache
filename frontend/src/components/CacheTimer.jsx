import { useEffect, useState } from 'react';
export default function CacheTimer({ cache }) {
 const [remaining, setRemaining] = useState(0);
 useEffect(() => {
  function tick() { setRemaining(Math.max(0, Math.ceil((cache.deadline - Date.now()) / 1000))); }
  tick();
  const timer = setInterval(tick, 250);
  return () => clearInterval(timer);
 }, [cache.deadline]);
 return <aside className={`cache ${remaining === 0 ? 'expired' : ''}`} aria-label="Estado de caché">
  <span className="dot"/><div><strong>{remaining > 0 ? `Caché: ${remaining} ${remaining === 1 ? 'segundo' : 'segundos'}` : 'Caché expirada'}</strong><small>{remaining > 0 ? 'Listado temporal en Redis' : 'Tus enlaces siguen guardados en MongoDB'}</small></div>
 </aside>;
}