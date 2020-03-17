namespace CorrWordAddIn
{

    public class Usuario
    {
        public string nombre { get; set; }
        public string cargo { get; set; }
        public string dependencia { get; set; }

        public Usuario(string nombre, string cargo, string dependencia)
        {
            this.nombre = nombre;
            this.cargo = cargo;
            this.dependencia = dependencia;
        }

        public Usuario()
        {

        }
    }
}
