package principal;

//Classe Paciente
public class Paciente {
	//atributos paciente
    private String cpf;
    private String nome;
    private String dataNascimento;
    private String prontuario;
    private double peso;
    private String idFuncionario;
    
    //contrutor paciente
    public Paciente(String cpf, String nome, String dataNascimento, String idFuncionario) {
        this.cpf = cpf;
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.prontuario = "";
        this.peso = 0;
        this.idFuncionario = idFuncionario;
    }

    //contrutor paciente
    public Paciente(String cpf, String nome, String dataNascimento, double peso,
        String idFuncionario) {
        this.cpf = cpf;
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.prontuario = "";
        this.peso = 0;
        this.idFuncionario = idFuncionario;
        this.peso = peso;
    }

    //contrutor paciente
    public Paciente(String cpf, String nome, double peso, String idFuncionario) {
        this.cpf = cpf;
        this.nome = nome;
        this.peso = peso;
        this.idFuncionario = idFuncionario;
    }

    //contrutor paciente
    public Paciente(String cpf, String nome, String idFuncionario) {
        this.cpf = cpf;
        this.nome = nome;
        this.peso = 0;
        this.idFuncionario = idFuncionario;
    }

    //contrutor paciente
    public Paciente() {
        this.cpf = "";
        this.dataNascimento = "";
        this.dataNascimento = "";
        this.nome = "";
        this.peso = 0;
        this.prontuario = "";
    }

    //Getters e Setters paciente
    public String getCpf() {
        return this.cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getPeso() {
        return this.peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public String getProntuario() {
        return this.prontuario;
    }

    public void setProntuario(String prontuario) {
        this.prontuario = prontuario;
    }

    public String getDataNascimento() {
        return this.dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    @Override
    public String toString() {
        return this.cpf + ";" + this.nome + ";" + this.peso + ";"
                + this.dataNascimento + ";" + this.prontuario + ";" + this.idFuncionario;
    }

    /**
     * @return String return the idFuncionario
     */
    public String getIdUsuario() {
        return idFuncionario;
    }

    /**
     * @param idFuncionario the idFuncionario to set
     */
    public void setIdUsuario(String idFuncionario) {
        this.idFuncionario = idFuncionario;
    }

}
