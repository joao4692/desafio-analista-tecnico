// Formato padrão de erro do back (GlobalExceptionHandler).
// Ex.: { status: 400, mensagem: "email: não deve estar em branco", ... }
export interface ApiErro {
  timestamp: string;
  status: number;
  erro: string;
  // Já vem em português e pronta para mostrar ao usuário.
  mensagem: string;
  caminho: string;
}
