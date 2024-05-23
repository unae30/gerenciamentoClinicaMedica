package principal;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import db.DB;
import db.DbException;
import db.DbIntegrityException;

//inicio classe controladorrrr
public class Controlador extends JFrame {
	
	//--------------------------- INÍCIO DA ORGANIZAÇÃO DA INTERFACE GRÁFICA ----------------------------
	//atributos para interface
    private static final long serialVersionUID = 1L;
	private JPanel mainPanel;
    private JPanel homePanel;
    private JPanel optionsPanel;
    private CardLayout cardLayout;
    private Connection conn;

    public Controlador(Connection conn) {

        this.conn = conn;

        setTitle("Gerenciamento de Clínica Médica");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);

        // Chamada da funçao para criar painel home
        createHomePanel();

        mainPanel.add(homePanel, "home");

        add(mainPanel);

        if (realizarLogin()) {
            showHomePage(); // Exibe a home page
            setTitle("Gerenciamento de Clínica Médica - User: "
                    + SessionManager.getInstance().getloggedUsuario().getNome());
            setVisible(true);

        } else {

            JOptionPane.showMessageDialog(this, "Login ou Senha invalidos. Encerrendo o programa.");
            System.exit(0);
        }
    }

    /* Metodo para realizar Login */
    private boolean realizarLogin() {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Bem vindo ao Gerenciamento de Clínica Médica");
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        loginPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(2, 2));

        JLabel loginLabel = new JLabel("Login:");
        JTextField loginField = new JTextField();
        formPanel.add(loginLabel);
        formPanel.add(loginField);

        JLabel passwordLabel = new JLabel("Senha:");
        JPasswordField passwordField = new JPasswordField();
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);

        loginPanel.add(formPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(null, loginPanel, "Login", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            String login = loginField.getText();
            String password = new String(passwordField.getPassword());

            PreparedStatement st = null;
            ResultSet rs = null;

            try {

                st = conn.prepareStatement(
                        "SELECT * FROM usuario WHERE login_acesso = ? and senha = ?");

                st.setString(1, login);

                st.setString(2, password);
                rs = st.executeQuery();
                if (rs.next()) {
                    Usuario obj = new Usuario();
                    obj.setCpf(rs.getString("cpf"));
                    obj.setNome(rs.getString("nome"));
                    obj.setLogin_acesso(rs.getString("login_acesso"));
                    obj.setSenha(rs.getString("senha"));
                    obj.setCargo(rs.getString("cargo"));
                    SessionManager.getInstance().setLoggedUsuario(obj);
                    JOptionPane.showMessageDialog(this, "Login autenticado!");
                    DB.closeStatement(st);

                    return true;
                }
            } catch (SQLException e) {
                throw new DbException(e.getMessage());
            }

        }

        return false;
    }
  


    //Método para autenticar o Administrador (médicos)
    private boolean autenticarAdmin() {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Autenticação de Médico");
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        loginPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(2, 2));

        JLabel loginLabel = new JLabel("Login:");
        JTextField loginField = new JTextField();
        formPanel.add(loginLabel);
        formPanel.add(loginField);

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);

        loginPanel.add(formPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(null, loginPanel, "Autenticação de Médico",
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            String login = loginField.getText();
            String password = new String(passwordField.getPassword());

            PreparedStatement st = null;
            ResultSet rs = null;

            try {
                st = conn.prepareStatement(
                        "SELECT * FROM usuario WHERE login_acesso = ? and senha = ? and cargo = ?");

                st.setString(1, login);
                st.setString(2, password);
                st.setString(3, "Médico");
                rs = st.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Login médico autenticado!");
                    DB.closeStatement(st);
                    return true;
                } else {
                    JOptionPane.showMessageDialog(this, "Erro ao autenticar!");
                }
            } catch (SQLException e) {
                throw new DbException(e.getMessage());
            }
        }
        return false;

    }

    //Método para criar o painel do menu principal
    private void createHomePanel() {

        homePanel = new JPanel();
        homePanel.setLayout(new BorderLayout());

        // Create the bottom panel for the "Área do Administrador" button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton adminButton = new JButton("Área do Médico");
        adminButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (SessionManager.getInstance().getloggedUsuario().getCargo().equals("Administrador")) {
                    abrirOpcoesAdmin();
                } else {
                    if (autenticarAdmin()) {
                        abrirOpcoesAdmin();
                    }
                }
            }
        });
        bottomPanel.add(adminButton);
        // Add the bottom panel to the page panel
        homePanel.add(bottomPanel, BorderLayout.SOUTH);

        // Create the center panel for the main buttons
        JPanel centerPanel = new JPanel(new GridLayout(0, 3, 20, 40)); // 3 columns, variable rows, 10px vertical and
                                                                       // horizontal gaps

        // Create botão Listar Registros
        JButton produtosButton = new JButton("Lista de Pacientes");
        produtosButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mostrarPaciente();
            }
        });
        centerPanel.add(produtosButton);

        // Novo botão Buscar paciente
        JButton buscarButton = new JButton("Buscar Paciente");
        buscarButton.setPreferredSize(new Dimension(50, 10));
        buscarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buscarRegistros();
            }
        });
        centerPanel.add(buscarButton);

        // Novo botão Listar pacientes por funcionário
        //Alterar a consulta de acordo com a data
        JButton listarCadFuncButton = new JButton("Paciente por Médico");
        listarCadFuncButton.setPreferredSize(new Dimension(50, 10));
        listarCadFuncButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listarPacientesPorUsuario();
            }
        });
        centerPanel.add(listarCadFuncButton);

        // Novo botão Encerrar Sistema
        JButton encerrarButton = new JButton("Encerrar Sessão");
        encerrarButton.setPreferredSize(new Dimension(50, 10));
        encerrarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                realizarLogin();
            }
        });
        centerPanel.add(encerrarButton);

        // Add the center panel to the page panel
        homePanel.add(centerPanel, BorderLayout.CENTER);
    }
    
    /* Metodo apenas para EXIBIR a pagina inicial */
    private void showHomePage() {
        cardLayout.show(mainPanel, "home");
    }
    
  //Método do painel da área de administradores (médicos)
    public void abrirOpcoesAdmin() {
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));

        JButton adicionarAdminButton = new JButton("Adicionar Funcionário");
        adicionarAdminButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        adicionarAdminButton.addActionListener(e -> adicionarUsuario());

        JButton listarAdminButton = new JButton("Listar Funcionários");
        listarAdminButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        listarAdminButton.addActionListener(e -> listarusuario());

        JButton editarAdminButton = new JButton("Editar funcionário");
        editarAdminButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        editarAdminButton.addActionListener(e -> editarUsuarioPorCPF());

        JButton removerAdminButton = new JButton("Remover funcionário");
        removerAdminButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removerAdminButton.addActionListener(e -> apagarUsuarioPorCPF());

        JButton alterarAdminButton = new JButton("Alterar prontuário médico");
        alterarAdminButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        alterarAdminButton.addActionListener(e -> alterarProntuarioPaciente());
        
        JButton addProdutoButton = new JButton("Adicionar Novo Paciente");
        addProdutoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addProdutoButton.addActionListener(e -> adicionarPaciente());
        
        JButton alterarButton = new JButton("Alterar Paciente");
        alterarButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        alterarButton.addActionListener(e -> atualizarRegistroPorCPF());
        
        JButton apagarButton = new JButton("Apagar Paciente");
        apagarButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        apagarButton.addActionListener(e -> apagarRegistroPorCPF());


        optionsPanel.add(Box.createVerticalStrut(20));
        optionsPanel.add(adicionarAdminButton);
        optionsPanel.add(Box.createVerticalStrut(20));
        optionsPanel.add(listarAdminButton);
        optionsPanel.add(Box.createVerticalStrut(20));
        optionsPanel.add(editarAdminButton);
        optionsPanel.add(Box.createVerticalStrut(20));
        optionsPanel.add(removerAdminButton);
        optionsPanel.add(Box.createVerticalStrut(20));
        optionsPanel.add(alterarAdminButton);
        optionsPanel.add(Box.createVerticalStrut(20));
        optionsPanel.add(alterarButton);
        optionsPanel.add(Box.createVerticalStrut(20));
        optionsPanel.add(addProdutoButton);
        optionsPanel.add(Box.createVerticalStrut(20));
        optionsPanel.add(apagarButton);

        // Set the preferred size to make the box 2 times bigger
        optionsPanel.setPreferredSize(
                new Dimension(optionsPanel.getPreferredSize().width, optionsPanel.getPreferredSize().height));

        JOptionPane.showOptionDialog(
                this,
                optionsPanel,
                "Opções de Administrador",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[] {},
                null);
    }
	//--------------------------- FIM ORGANIZAÇÃO DA INTERFACE GRÁFICA ----------------------------

    //----------------------------- INÍCIO MÉTODOS DAS FUNCIONALIDADES----------------------------------------------
    //Método para listar pacientes por usário
    private void listarPacientesPorUsuario() {

        String searchQuery = JOptionPane.showInputDialog(this, "Digite o CPF do médico para busca:");

        if (searchQuery == null) {
            // User canceled the input or closed the dialog
            return;
        }

        searchQuery = searchQuery.replaceAll("[^0-9]", ""); // Remove non-numeric characters
        searchQuery = formatCPF(searchQuery);
        // Create the table model

        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            st = conn.prepareStatement(
                    "SELECT p.identificacao, p.nome_paciente, p.prontuario, u.cpf, u.nome, u.cargo FROM paciente p INNER JOIN usuario u ON p.cpf_usuario = u.cpf WHERE cpf_usuario = ?");
            st.setString(1, searchQuery);
            rs = st.executeQuery();
            JPanel panel = new JPanel(new BorderLayout());

            // Crie a tabela e o modelo
            DefaultTableModel tableModel = new DefaultTableModel();
            tableModel.setColumnIdentifiers(
                    new Object[] { "CPF", "Nome do Paciente", "Prontuário" });
            // Crie a JTable

            while (rs.next()) {

                Object[] rowData = {
                        rs.getString("identificacao"),
                        rs.getString("nome_paciente"),
                        rs.getString("prontuario"),
                };
                tableModel.addRow(rowData);
                String nome_func = rs.getString("nome");
                String cargo = rs.getString("cargo");
                // Crie a JTable
                JTable table = new JTable(tableModel);
                JScrollPane scrollPane = new JScrollPane(table);
                JLabel infoLabel = new JLabel("Funcionário: " + nome_func + " | Cargo: " + cargo);
                panel.add(infoLabel, BorderLayout.NORTH);
                // Adicione o scrollPane com a tabela ao JPanel
                panel.add(scrollPane, BorderLayout.CENTER);

            }

            // Crie e exiba o diálogo com o JPanel
            JDialog dialog = new JDialog(this, "Listagem de pacientes registrados por funcionário", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.getContentPane().add(panel); // Adicione o JPanel ao diálogo
            dialog.pack();

            // Defina o tamanho do diálogo
            dialog.setSize(900, 500);

            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (

        SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    //Método para listar usuarios
    private void listarusuario() {

        // Create the table model
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(
                new Object[] { "CPF", "Nome", "Login", "Cargo" });

        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            st = conn.prepareStatement("SELECT * FROM usuario ORDER BY nome");
            rs = st.executeQuery();

            while (rs.next()) {
                Object[] rowData = {
                        rs.getString("cpf"),
                        rs.getString("nome"),
                        rs.getString("login_acesso"),
                        rs.getString("cargo"),
                };
                tableModel.addRow(rowData);
            }

            // Create the table and scroll pane
            JTable table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);

            // Create the dialog and display the table
            JDialog dialog = new JDialog(this, "Funcionários", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.getContentPane().add(scrollPane);
            dialog.pack();

            // Set the size of the dialog
            dialog.setSize(900, 500);

            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Nenhum funcionário registrado");
            throw new DbException(e.getMessage());
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    //Método para mostrar dados pacientes
    private void mostrarPaciente() {

        // Create the table model
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(
                new Object[] { "CPF", "Nome", "Peso", "Data de nascimento", "Prontuário",
                        "ID do funcionário" });

        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            st = conn.prepareStatement("SELECT * FROM paciente ORDER BY nome_paciente");
            rs = st.executeQuery();

            while (rs.next()) {
                Object[] rowData = {
                        rs.getString("identificacao"),
                        rs.getString("nome_paciente"),
                        rs.getDouble("peso"),
                        rs.getString("datanascimento"),
                        rs.getString("prontuario"),
                        rs.getString("cpf_usuario")
                };
                tableModel.addRow(rowData);
            }

            // Create the table and scroll pane
            JTable table = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(table);

            // Create the dialog and display the table
            JDialog dialog = new JDialog(this, "Pacientes", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.getContentPane().add(scrollPane);
            dialog.pack();

            // Set the size of the dialog
            dialog.setSize(900, 500);

            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    //Método para criar um novo paciente
    private void adicionarPaciente() {
        JTextField cpfField = new JTextField(15);
        JTextField nomeField = new JTextField(15);
        JTextField pesoField = new JTextField(15);
        JTextField dataNascimentoField = new JTextField(15);

        // Set placeholder for CPF field
        cpfField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (cpfField.getText().equals("Apenas Números")) {
                    cpfField.setText("");
                    cpfField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (cpfField.getText().isEmpty()) {
                    cpfField.setText("Apenas Números");
                    cpfField.setForeground(Color.GRAY);
                }
            }
        });
        cpfField.setText("Apenas Números");
        cpfField.setForeground(Color.GRAY);

        // Set placeholder for Data de Nascimento\s field
        dataNascimentoField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (dataNascimentoField.getText().equals("DD/MM/YYYY")) {
                    dataNascimentoField.setText("");
                    dataNascimentoField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (dataNascimentoField.getText().isEmpty()) {
                    dataNascimentoField.setText("DD/MM/YYYY");
                    dataNascimentoField.setForeground(Color.GRAY);
                }
            }
        });
        dataNascimentoField.setText("DD/MM/YYYY");
        dataNascimentoField.setForeground(Color.GRAY);

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("CPF:"));
        panel.add(cpfField);
        panel.add(new JLabel("Nome:"));
        panel.add(nomeField);
        panel.add(new JLabel("Peso:"));
        panel.add(pesoField);
        panel.add(new JLabel("Data de Nascimento:"));
        panel.add(dataNascimentoField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Adicionar Paciente", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String cpf = cpfField.getText().replaceAll("[^0-9]", ""); // Remove non-numeric characters from the input
            String nome = nomeField.getText();
            double peso = Double.parseDouble(pesoField.getText());
            String dataNascimento = dataNascimentoField.getText();
            String idusuario = SessionManager.getInstance().getloggedUsuario().getCpf();

            if (confirmarEntrada(cpf, nome, peso, dataNascimento, idusuario)) {
                // Show a confirmation dialog before adding the record
                String message = "Deseja adicionar o seguinte paciente?\n\n"
                        + "CPF: " + formatCPF(cpf) + "\n"
                        + "Nome: " + nome + "\n"
                        + "Peso: " + peso + "\n"
                        + "Data da Nascimento: " + dataNascimento + "\n"
                        + "ID do funcionário: " + idusuario;

                int confirmation = JOptionPane.showConfirmDialog(this, message, "Confirmação",
                        JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {
                    Paciente corpo = new Paciente(formatCPF(cpf), nome, dataNascimento, peso,
                            idusuario);

                    // ---------- CONSULTA BANCO DE DADOS ---------------------
                    PreparedStatement st = null;
                    try {
                        st = conn.prepareStatement(
                                "INSERT INTO paciente " +
                                        "(identificacao, nome_paciente, peso, dataNascimento, prontuario, cpf_usuario) "
                                        +
                                        "VALUES " +
                                        "(?, ?, ?, ?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS);

                        st.setString(1, corpo.getCpf());
                        st.setString(2, corpo.getNome());
                        st.setDouble(3, corpo.getPeso());
                        st.setString(4, corpo.getDataNascimento());
                        st.setString(5, corpo.getProntuario());
                        st.setString(6, SessionManager.getInstance().getloggedUsuario().getCpf());

                        int rowsAffected = st.executeUpdate();

                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(this, "Registro inserido com sucesso!");
                        } else {
                            JOptionPane.showMessageDialog(this, "Erro ao inserir!");

                        }
                    } catch (SQLException e) {
                        throw new DbException(e.getMessage());
                    } finally {
                        DB.closeStatement(st);
                    }
                }
            }
        }

    }

    /*--------Metodo para ATUALIZAR paciente--------*/
    public void atualizarRegistroPorCPF() {
        JTextField cpfField = new JTextField(15);

        cpfField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (cpfField.getText().equals("Apenas Números")) {
                    cpfField.setText("");
                    cpfField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (cpfField.getText().isEmpty()) {
                    cpfField.setText("Apenas Números");
                    cpfField.setForeground(Color.GRAY);
                }
            }
        });
        cpfField.setText("Apenas Números");
        cpfField.setForeground(Color.GRAY);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Digite o CPF para alterar dados do registro:"));
        panel.add(cpfField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Digite o CPF", JOptionPane.OK_CANCEL_OPTION);

        String searchQuery = cpfField.getText();

        if (searchQuery == null || searchQuery.equals("Apenas Números")) {
            return;
        }
        if (result == JOptionPane.OK_OPTION) {
            searchQuery = searchQuery.replaceAll("[^0-9]", "");
            searchQuery = formatCPF(searchQuery);

            PreparedStatement st = null;
            ResultSet rs = null;
            // Aqui retorna todos os dados do cpf que buscou
            try {
                st = conn.prepareStatement(
                        "SELECT * FROM paciente WHERE identificacao = ?");
                st.setString(1, searchQuery);

                rs = st.executeQuery();
                if (rs.next()) {
                    String updatedName = JOptionPane.showInputDialog(this, "Digite o novo nome:",
                            rs.getString("nome_paciente"));
                    String updatedWeight = JOptionPane.showInputDialog(this, "Digite o novo peso:",
                            rs.getDouble("peso"));
                    String updatedBirthDate = JOptionPane.showInputDialog(this, "Digite a nova data de nascimento:",
                            rs.getString("dataNascimento"));
                    if (updatedName != null && !updatedName.isEmpty() && updatedWeight != null
                            && !updatedWeight.isEmpty() && updatedBirthDate != null && !updatedBirthDate.isEmpty()) {
                        double updatedWeightD = Double.parseDouble(updatedWeight);
                        // ---------- CONSULTA BANCO DE DADOS ---------------------
                        // Aqui atualiza todos os dados do cpf inserido
                        try {
                            st = conn.prepareStatement(

                                    "UPDATE paciente SET nome_paciente = ?, peso = ?, dataNascimento = ? WHERE identificacao = ?",
                                    Statement.RETURN_GENERATED_KEYS);

                            st.setString(1, updatedName);
                            st.setDouble(2, updatedWeightD);
                            st.setString(3, updatedBirthDate);
                            st.setString(5, searchQuery);

                            int rowsAffected = st.executeUpdate();

                            if (rowsAffected > 0) {
                                JOptionPane.showMessageDialog(this, "Registro atualizado!");
                            } else {
                                JOptionPane.showMessageDialog(this, "Erro ao inserir!");
                            }
                        } catch (SQLException e) {
                            throw new DbException(e.getMessage());
                        } finally {
                            DB.closeStatement(st);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Os registros não podem ser vazios.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "CPF não encontrado.");
            }
        }
    }

    //Método que formata o CPF
    private String formatCPF(String cpf) {
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    //Método que pede confirmação dos campos para o usuário
    private boolean confirmarCampos(String cpf, String nome, String login_acesso, String senha, String cargo) {
        if (cpf.length() < 10 || cpf.isEmpty()) {
            return false;
        }

        if (nome.isEmpty() && login_acesso.isEmpty() && senha.isEmpty() && cargo.isEmpty()) {
            return false;
        }
        return true;
    }

  //Método que pede confirmação das entradas do usuário
    private boolean confirmarEntrada(String cpf, String nome, Double peso, String dataNascimento,
             String idusuario) {
        if (cpf.length() < 10 || cpf.isEmpty()) {
            return false;
        }

        if (nome.isEmpty() && peso == 0 && dataNascimento.isEmpty() ) {
            return false;
        }

        return true;
    }

    //Método para consultar pacientes (podendo ser por nome ou cpf)
    public void buscarRegistros() {
        // Show the option dialog with the buttons
        int option = JOptionPane.showOptionDialog(
                this,
                "Escolha o parâmetro de busca",
                "Buscar Pacientes",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[] { "Nome", "CPF" },
                null);

        // Check the user's choice and perform the search
        if (option == 0) {
            buscarRegistrosPorNome();
        } else if (option == 1) {
            buscarRegistrosPorCPF();
        } else {
            // If the user closes the dialog or doesn't make a selection, return
            return;
        }
    }

    //Método para buscar pacientes através dos nome, se escolher por nome na função acima.
    public void buscarRegistrosPorNome() {
        // Ask the user for the search query
        String searchQuery = JOptionPane.showInputDialog(this, "Digite o Nome para buscar:");

        if (searchQuery == null) {
            // User canceled the input or closed the dialog
            return;
        }
        PreparedStatement st = null;
        ResultSet rs = null;
        DefaultTableModel tableModel = new DefaultTableModel(
                new Object[] { "CPF", "Nome", "Peso", "Data da nascimento", "Prontuário",
                        "ID do funcionário" },
                0);

        try {
            st = conn.prepareStatement("SELECT * FROM paciente WHERE nome_paciente = ?");
            st.setString(1, searchQuery);
            rs = st.executeQuery();

            while (rs.next()) {
                Object[] rowData = {
                        rs.getString("identificacao"),
                        rs.getString("nome_paciente"),
                        rs.getDouble("peso"),
                        rs.getString("dataNascimento"),
                        rs.getString("prontuario"),
                        rs.getString("cpf_usuario"),
                };
                tableModel.addRow(rowData);
            }

            // Aqui você precisa criar e configurar a tabela para exibir o modelo
            JTable table = new JTable(tableModel);

            // Adicione a tabela a um JScrollPane para permitir rolagem, se necessário
            JScrollPane scrollPane = new JScrollPane(table);

            // Agora você pode adicionar o scrollPane à sua interface gráfica
            // por exemplo, a um JPanel ou JFrame

            // Create the dialog and display the table
            JDialog dialog = new JDialog(this, "Pacientes", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.getContentPane().add(scrollPane);
            dialog.pack();

            // Set the size of the dialog
            dialog.setSize(900, 500);

            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }

        catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    //Método para buscar pacientes através dos cpfs, se escolher por CPF na função acima.
    public void buscarRegistrosPorCPF() {

        String searchQuery = JOptionPane.showInputDialog(this, "Digite o CPF para buscar:");

        if (searchQuery == null) {
            // User canceled the input or closed the dialog
            return;
        }
        searchQuery = searchQuery.replaceAll("[^0-9]", ""); // Remove non-numeric characters
        searchQuery = formatCPF(searchQuery);
        PreparedStatement st = null;
        ResultSet rs = null;
        DefaultTableModel tableModel = new DefaultTableModel(
                new Object[] { "CPF", "Nome", "Peso", "Data da nascimento", "Prontuário",
                        "ID do funcionário" },
                0);

        try {
            st = conn.prepareStatement("SELECT * FROM paciente WHERE identificacao = ?");
            st.setString(1, searchQuery);
            rs = st.executeQuery();

            while (rs.next()) {
                Object[] rowData = {
                        rs.getString("identificacao"),
                        rs.getString("nome_paciente"),
                        rs.getDouble("peso"),
                        rs.getString("dataNascimento"),
                        rs.getString("prontuario"),
                        rs.getString("cpf_usuario"),
                };
                tableModel.addRow(rowData);
            }

            // Aqui você precisa criar e configurar a tabela para exibir o modelo
            JTable table = new JTable(tableModel);

            // Adicione a tabela a um JScrollPane para permitir rolagem, se necessário
            JScrollPane scrollPane = new JScrollPane(table);

            // Agora você pode adicionar o scrollPane à sua interface gráfica
            // por exemplo, a um JPanel ou JFrame

            // Create the dialog and display the table
            JDialog dialog = new JDialog(this, "Pacientes", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.getContentPane().add(scrollPane);
            dialog.pack();

            // Set the size of the dialog
            dialog.setSize(900, 500);

            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }

        catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    //Método para apagar registros de pacientes por cpf
    public void apagarRegistroPorCPF() {

        JTextField cpfField = new JTextField(15);

        // Set placeholder for CPF field
        cpfField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (cpfField.getText().equals("Apenas Números")) {
                    cpfField.setText("");
                    cpfField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (cpfField.getText().isEmpty()) {
                    cpfField.setText("Apenas Números");
                    cpfField.setForeground(Color.GRAY);
                }
            }
        });
        cpfField.setText("Apenas Números");
        cpfField.setForeground(Color.GRAY);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Digite o CPF para apagar o registro:"));
        panel.add(cpfField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Digite o CPF", JOptionPane.OK_CANCEL_OPTION);

        String searchQuery = cpfField.getText();

        if (searchQuery == null) {
            // User canceled the input or closed the dialog
            return;
        }

        if (result == JOptionPane.OK_OPTION) {
            searchQuery = searchQuery.replaceAll("[^0-9]", ""); // Remove non-numeric characters
            searchQuery = formatCPF(searchQuery);

            PreparedStatement st = null;
            ResultSet rs = null;

            try {
                st = conn.prepareStatement(
                        "SELECT * FROM paciente WHERE identificacao = ?");

                st.setString(1, searchQuery);
                rs = st.executeQuery();
                if (rs.next()) {
                    Paciente obj = new Paciente();
                    obj.setCpf(rs.getString("identificacao"));
                    obj.setNome(rs.getString("nome_paciente"));
                    obj.setProntuario(rs.getString("prontuario"));
                    obj.setIdUsuario(rs.getString("cpf_usuario"));
                    int confirmation = JOptionPane.showConfirmDialog(this,
                            "Deseja apagar o registro com o CPF: " + searchQuery + "?" +
                                    "\nNome: " + obj.getNome() + "\nProntuário: " + obj.getProntuario()
                                    + "\nID funcionário: "
                                    + obj.getIdUsuario(),
                            "Confirmação", JOptionPane.YES_NO_OPTION);
                    // PreparedStatement st = null;
                    st.close();
                    if (confirmation == JOptionPane.YES_OPTION) {
                        st = conn.prepareStatement(
                                "DELETE FROM paciente WHERE identificacao = ?");
                        st.setString(1, searchQuery);
                        st.executeUpdate();
                        DB.closeStatement(st);
                        JOptionPane.showMessageDialog(this, "Registro apagado com sucesso!");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "CPF não encontrado!");
                }
            } catch (SQLException e) {
                throw new DbIntegrityException(e.getMessage());
            }

        }
    }

    //Método para alterar valor de prontuário dos pacientes
    public void alterarProntuarioPaciente() {
        JTextField cpfField = new JTextField(15);

        cpfField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (cpfField.getText().equals("Apenas Números")) {
                    cpfField.setText("");
                    cpfField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (cpfField.getText().isEmpty()) {
                    cpfField.setText("Apenas Números");
                    cpfField.setForeground(Color.GRAY);
                }
            }
        });
        cpfField.setText("Apenas Números");
        cpfField.setForeground(Color.GRAY);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Digite o CPF para alterar o prontuário:"));
        panel.add(cpfField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Digite o CPF", JOptionPane.OK_CANCEL_OPTION);

        String searchQuery = cpfField.getText();

        if (searchQuery == null || searchQuery.equals("Apenas Números")) {
            return;
        }

        if (result == JOptionPane.OK_OPTION) {
            searchQuery = searchQuery.replaceAll("[^0-9]", "");
            searchQuery = formatCPF(searchQuery);

            PreparedStatement st = null;
            ResultSet rs = null;
            try {
                st = conn.prepareStatement(
                        "SELECT prontuario FROM paciente WHERE identificacao = ?");
                st.setString(1, searchQuery);

                rs = st.executeQuery();
                if (rs.next()) {
                    String updatedSituation = JOptionPane.showInputDialog(this, "Digite o novo prontuário:",
                            rs.getString("prontuario"));

                    if (updatedSituation != null && !updatedSituation.isEmpty()) {
                        try {
                            st = conn.prepareStatement(

                                    "UPDATE paciente SET prontuario = ? WHERE identificacao = ?",
                                    Statement.RETURN_GENERATED_KEYS);

                            st.setString(1, updatedSituation);
                            st.setString(2, searchQuery);

                            int rowsAffected = st.executeUpdate();

                            if (rowsAffected > 0) {
                                JOptionPane.showMessageDialog(null, "Prontuário atualizado com sucesso!");

                            } else {
                                throw new DbException("Erro ao inserir!");
                            }
                        } catch (SQLException e) {

                            throw new DbException(e.getMessage());
                        }
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "CPF não encontrado");
            }
        }

    }

    

    //Método para editar dados dos usuários do sistemas, incluindo login e senha
    private void editarUsuarioPorCPF() {
        JTextField cpfField = new JTextField(15);

        cpfField.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                if (cpfField.getText().equals("Apenas Números")) {
                    cpfField.setText("");
                    cpfField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (cpfField.getText().isEmpty()) {
                    cpfField.setText("Apenas Números");
                    cpfField.setForeground(Color.GRAY);
                }
            }
        });
        cpfField.setText("Apenas Números");
        cpfField.setForeground(Color.GRAY);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Digite o CPF para alterar dados do funcionário:"));
        panel.add(cpfField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Digite o CPF",
                JOptionPane.OK_CANCEL_OPTION);

        String searchQuery = cpfField.getText();

        if (searchQuery == null || searchQuery.equals("Apenas Números")) {
            return;
        }
        if (result == JOptionPane.OK_OPTION) {
            searchQuery = searchQuery.replaceAll("[^0-9]", "");
            searchQuery = formatCPF(searchQuery);

            PreparedStatement st = null;
            ResultSet rs = null;
            // Aqui retorna todos os dados do cpf que buscou
            try {
                st = conn.prepareStatement(
                        "SELECT * FROM usuario WHERE cpf = ?");
                st.setString(1, searchQuery);

                rs = st.executeQuery();
                if (rs.next()) {
                    String updatedName = JOptionPane.showInputDialog(this, "Digite o novo nome:",
                            rs.getString("nome"));
                    String updatedLogin = JOptionPane.showInputDialog(this,
                            "Digite o novo login:",
                            rs.getString("login_acesso"));
                    String updatedPassword = JOptionPane.showInputDialog(this,
                            "Digite a nova senha:",
                            rs.getString("senha"));
                    String updatedCargo = JOptionPane.showInputDialog(this,
                            "Digite o novo cargo:",
                            rs.getString("cargo"));
                    if (updatedName != null && !updatedName.isEmpty() && updatedLogin != null
                            && !updatedLogin.isEmpty() && updatedPassword != null &&
                            !updatedPassword.isEmpty()
                            && updatedCargo != null && !updatedCargo.isEmpty()) {
                        // ---------- CONSULTA BANCO DE DADOS ---------------------
                        // Aqui atualiza todos os dados do cpf inserido
                        try {
                            st = conn.prepareStatement(

                                    "UPDATE usuario SET nome= ?, login_acesso = ?, senha = ?, cargo = ?  WHERE cpf = ?",
                                    Statement.RETURN_GENERATED_KEYS);

                            st.setString(1, updatedName);
                            st.setString(2, updatedLogin);
                            st.setString(3, updatedPassword);
                            st.setString(4, updatedCargo);
                            st.setString(5, searchQuery);

                            int rowsAffected = st.executeUpdate();

                            if (rowsAffected > 0) {
                                JOptionPane.showMessageDialog(this, "Funcionário atualizado!");
                            } else {
                                JOptionPane.showMessageDialog(this, "Erro ao inserir!");
                            }
                        } catch (SQLException e) {
                            throw new DbException(e.getMessage());
                        } finally {
                            DB.closeStatement(st);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Os registros não podem ser vazios.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "CPF não encontrado.");
            }
        }
    }

    // Adicionando um usuario na aba administração
    private void adicionarUsuario() {
        JTextField cpfField = new JTextField(15);
        JTextField nomeField = new JTextField(15);
        JTextField login_acessoField = new JTextField(15);
        JPasswordField senhaField = new JPasswordField(15);
        JTextField cargoField = new JTextField(15);

        // Set placeholder for CPF field
        cpfField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (cpfField.getText().equals("Apenas Números")) {
                    cpfField.setText("");
                    cpfField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (cpfField.getText().isEmpty()) {
                    cpfField.setText("Apenas Números");
                    cpfField.setForeground(Color.GRAY);
                }
            }
        });
        cpfField.setText("Apenas Números");
        cpfField.setForeground(Color.GRAY);

        // Set placeholder for Hora de Nascimento field
        senhaField.setEchoChar('*'); // Define o caractere de eco como '*'

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("CPF:"));
        panel.add(cpfField);
        panel.add(new JLabel("Nome:"));
        panel.add(nomeField);
        panel.add(new JLabel("Login:"));
        panel.add(login_acessoField);
        panel.add(new JLabel("Criar senha:"));
        panel.add(senhaField);
        panel.add(new JLabel("Cargo:"));
        panel.add(cargoField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Adicionar Funcionário", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String cpf = cpfField.getText().replaceAll("[^0-9]", ""); // Remove non-numeric characters from the input
            String nome = nomeField.getText();
            String login_acesso = login_acessoField.getText();
            // String senha = senhaField.getText();
            char[] senhaChars = senhaField.getPassword();
            String senha = new String(senhaChars);
            String cargo = cargoField.getText();

            if (confirmarCampos(cpf, nome, login_acesso, senha, cargo)) {
                // Show a confirmation dialog before adding the record
                String message = "Deseja adicionar o seguinte Funcionário?\n\n"
                        + "CPF: " + formatCPF(cpf) + "\n"
                        + "Nome: " + nome + "\n"
                        + "Login: " + login_acesso + "\n"
                        + "Senha: " + senha + "\n"
                        + "Cargo: " + cargo + "\n";

                int confirmation = JOptionPane.showConfirmDialog(this, message, "Confirmação",
                        JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {
                    Usuario usuario = new Usuario(formatCPF(cpf), nome, login_acesso, senha, cargo);
                    PreparedStatement st = null;
                    try {
                        st = conn.prepareStatement(
                                "INSERT INTO usuario " +
                                        "(cpf, nome, login_acesso, senha, cargo)" +
                                        "VALUES " +
                                        "(?, ?, ?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS);

                        st.setString(1, usuario.getCpf());
                        st.setString(2, usuario.getNome());
                        st.setString(3, usuario.getLogin_acesso());
                        st.setString(4, usuario.getSenha());
                        st.setString(5, usuario.getCargo());

                        int rowsAffected = st.executeUpdate();

                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(this, "Funcionário adicionado com sucesso!");
                        } else {
                            throw new DbException("Erro ao inserir!");
                        }
                    } catch (SQLException e) {
                        throw new DbException(e.getMessage());
                    } finally {
                        DB.closeStatement(st);
                    }
                }

            }
        } else {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos antes de adicionar o funcionário.");
        }
    }

    //Método para excluir um usuário atraves do cpf
    public void apagarUsuarioPorCPF() {
        JTextField cpfField = new JTextField(15);

        // Adicionar o FocusListener ao campo de CPF
        cpfField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (cpfField.getText().equals("Apenas Números")) {
                    cpfField.setText("");
                    cpfField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (cpfField.getText().isEmpty()) {
                    cpfField.setText("Apenas Números");
                    cpfField.setForeground(Color.GRAY);
                }
            }
        });

        // Configurar o placeholder e cor do texto
        cpfField.setText("Apenas Números");
        cpfField.setForeground(Color.GRAY);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Digite o CPF para apagar o funcionário:"));
        panel.add(cpfField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Digite o CPF", JOptionPane.OK_CANCEL_OPTION);

        String searchQuery = cpfField.getText();

        if (searchQuery == null) {
            // Usuário cancelou a entrada ou fechou a caixa de diálogo
            return;
        }

        if (result == JOptionPane.OK_OPTION) {
            searchQuery = searchQuery.replaceAll("[^0-9]", ""); // Remove caracteres não numéricos
            searchQuery = formatCPF(searchQuery);

            // Verificar se o CPF está vazio
            if (searchQuery.isEmpty()) {
                JOptionPane.showMessageDialog(this, "CPF inválido.");
                return;
            }

            PreparedStatement st = null;
            ResultSet rs = null;

            try {
                st = conn.prepareStatement(
                        "SELECT * FROM usuario WHERE cpf = ?");

                st.setString(1, searchQuery);
                rs = st.executeQuery();
                if (rs.next()) {
                    Usuario obj = new Usuario();
                    obj.setCpf(rs.getString("cpf"));
                    obj.setNome(rs.getString("nome"));
                    int confirmation = JOptionPane.showConfirmDialog(this,
                            "Deseja apagar o funcionário com o CPF: " + searchQuery + "?" +
                                    "\nNome: " + obj.getNome(),
                            "Confirmação", JOptionPane.YES_NO_OPTION);
                    // PreparedStatement st = null;
                    st.close();
                    if (confirmation == JOptionPane.YES_OPTION) {
                        st = conn.prepareStatement(
                                "DELETE FROM usuario WHERE cpf = ?");
                        st.setString(1, searchQuery);
                        st.executeUpdate();
                        DB.closeStatement(st);
                        JOptionPane.showMessageDialog(this, "Funcionário apagado com sucesso!");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "CPF não encontrado!");
                }
            } catch (SQLException e) {
                throw new DbIntegrityException(e.getMessage());

            }
        }
    }
    
    //------------------------------------- FIM DAS FUNÇÕES DE FUNCIONALIDADE------------------------------------------

    //--------------INICIO MÉTODO MAIN------------------------
    public static void main(String[] args) {
        Connection conn = DB.getConnection();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Controlador(conn);
            }
        });
    }
    //-----------------FIM MÉTODO MAIN------------------------
}//FIM DA CLASSE CONTROLADOR
