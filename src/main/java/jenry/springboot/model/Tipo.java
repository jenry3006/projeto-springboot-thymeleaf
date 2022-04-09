package jenry.springboot.model;

public enum Tipo {

    FISICA ("Pessoa física"),
    JURIDICA("Pessoa Jurídica");

    private String nome;
    private String valor;

    private Tipo(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getValor() {
        return valor = this.name();
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return this.name();
    }
}
