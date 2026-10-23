import { useState, useEffect, useRef } from 'react';
export default function CopyButton({ url }) {
 const [message, setMessage] = useState('');
 const timeout = useRef();
 useEffect(() => () => clearTimeout(timeout.current), []);
 async function copy() {
  try { await navigator.clipboard.writeText(url); setMessage('Enlace copiado'); }
  catch { setMessage('No se pudo copiar. Selecciona el enlace y usa Ctrl+C.'); }
  clearTimeout(timeout.current);
  timeout.current = setTimeout(() => setMessage(''), 3500);
 }
 return <div className="copy"><button type="button" className="secondary small" onClick={copy}>Copiar</button>{message && <span role="status">{message}</span>}</div>;
}