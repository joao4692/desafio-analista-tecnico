// Endereço da API usado por todos os services.
// É relativo: em dev, o proxy do Angular repassa /api para o back em localhost:8080;
// no Docker, o nginx faz o mesmo. Assim, o código funciona no PC, no tablet e no servidor.
export const environment = {
  apiUrl: '/api',
};
