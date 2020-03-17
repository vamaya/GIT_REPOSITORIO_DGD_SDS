namespace CorrWordAddIn
{/// <summary>
/// Clase inf Paises
/// </summary>
    public class Pais
    {
        public string idPais { get; set; }
        public string nombrePais { get; set; }

        public Pais(string idPais, string nombrePais)
        {
            this.idPais = idPais;
            this.nombrePais = nombrePais;
        }
    }
}