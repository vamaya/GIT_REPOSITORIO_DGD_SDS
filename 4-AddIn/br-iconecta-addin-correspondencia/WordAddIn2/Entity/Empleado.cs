using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CorrWordAddIn
{
    public class Empleado : Usuario
    {
        public string idFirmante { get; set; }
        public string cargoIngles { get; set; }
        public int firmaInt { get; set; }
        public int firmaExt { get; set; }
        public string sigla { get; set; }
        public string dependenciaIngles { get; set; }
        public int fondoIndependiente { get; set; }
        public string pcrNormal { get; set; }
        public string idPcrNormal { get; set; }
        public string pcrConfidencial { get; set; }
        public string idPcrConfidencial { get; set; }
        public string cdd { get; set; }
        public string idCdd { get; set; }
        public string ciudad { get; set; }
        public string direccion { get; set; }
        public string telefono { get; set; }

        public Empleado()
        {

        }



    }
}
