using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using Word = Microsoft.Office.Interop.Word;

/// <summary>
/// Clase carga informacion de los destinos
/// </summary>

namespace CorrWordAddIn
{
    public partial class DatosDestinosForm : Form
    {


        public DatosDestinosForm()
        {
            InitializeComponent();
        }


        private void DatosDestinosForm_Load_1(object sender, EventArgs e)
        {
            DataGridViewTextBoxColumn c1 = new DataGridViewTextBoxColumn();
            c1.HeaderText = "Nombre";
            c1.Width = 185;
            c1.ReadOnly = true;

            DataGridViewTextBoxColumn c2 = new DataGridViewTextBoxColumn();
            c2.HeaderText = "Entidad";
            c2.Width = 185;
            c2.ReadOnly = true;

            DataGridViewTextBoxColumn c3 = new DataGridViewTextBoxColumn();
            c3.HeaderText = "Dirección (Física / Correo Electrónico)";
            c3.Width = 240;
            c3.ReadOnly = true;

            dgvDestinos.Columns.Add(c1);
            dgvDestinos.Columns.Add(c2);
            dgvDestinos.Columns.Add(c3);


            DataGridViewTextBoxColumn c4 = new DataGridViewTextBoxColumn();
            c4.HeaderText = "Nombre";
            c4.Width = 185;
            c4.ReadOnly = true;

            DataGridViewTextBoxColumn c5 = new DataGridViewTextBoxColumn();
            c5.HeaderText = "Entidad";
            c5.Width = 185;
            c5.ReadOnly = true;

            DataGridViewTextBoxColumn c6 = new DataGridViewTextBoxColumn();
            c6.HeaderText = "Dirección (Física / Correo Electrónico)";
            c6.Width = 240;
            c6.ReadOnly = true;

            dgvCopias.Columns.Add(c4);
            dgvCopias.Columns.Add(c5);
            dgvCopias.Columns.Add(c6);


            ServicesCarta.insertDataForm(dgvDestinos, Globals.ThisAddIn.listaDestinosCa);
            ServicesCarta.insertDataForm(dgvCopias, Globals.ThisAddIn.listaCopiasCa);
        }
    }

}

