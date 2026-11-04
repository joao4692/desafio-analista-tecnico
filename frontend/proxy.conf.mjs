// Proxy do ng serve: o navegador (no PC ou no tablet) só fala com o Angular,
// e as chamadas para /api seguem para o back na própria máquina.
export default {
  '/api': {
    target: 'http://localhost:8080',
    secure: false,
    changeOrigin: true,
    // Pelo proxy a chamada é da mesma origem. Sem tirar o Origin, o back aplicaria o CORS
    // e recusaria (403) POST, PUT, PATCH e DELETE vindos do tablet (192.168.x.x:4200).
    configure: (proxy) => {
      proxy.on('proxyReq', (proxyReq) => proxyReq.removeHeader('origin'));
    },
  },
};
