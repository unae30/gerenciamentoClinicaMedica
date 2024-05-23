package principal;

    public class SessionManager {
        private static SessionManager instance;
        private Usuario loggedUsuario;
    
        private SessionManager() {
            // Privado para garantir que apenas uma instância seja criada
        }
    
        public static SessionManager getInstance() {
            if (instance == null) {
                instance = new SessionManager();
            }
            return instance;
        }
    
        public void setLoggedUsuario(Usuario usuario) {
            loggedUsuario = usuario;
        }
    
        public Usuario getloggedUsuario() {
            return loggedUsuario;
        }
}
