using Microsoft.Office.Interop.Word;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace CorrWordAddIn
{
    public partial class SearchForm : Form
    {
        public bool primerDestAgregado = false;
        public bool primerCopiaAgregado = false;
        public bool primerRemitenteAgregado = false;
        public bool primerMarcaAgregada = false;
        DataView vistaUsuarios;

        public SearchForm()
        {
            InitializeComponent();
            cargarGrilla();
        }

        /// <summary>
        /// Carga la tabla con la información del arreglo arrUsuarios.
        /// </summary>
        public void cargarGrilla()
        {
            // Create one DataTable with one column.
            System.Data.DataTable tblUsuarios = new System.Data.DataTable("table");
            DataColumn colIdFirmanteBuscar = new DataColumn("IdFirmanteBuscar", Type.GetType("System.String"));
            DataColumn colNombre = new DataColumn("Nombre", Type.GetType("System.String"));
            DataColumn colCargo = new DataColumn("Cargo", Type.GetType("System.String"));
            DataColumn colFirmanteBuscar = new DataColumn("FirmanteBuscar", Type.GetType("System.String"));
            DataColumn colSiglaFirmanteBuscar = new DataColumn("SiglaFirmanteBuscar", Type.GetType("System.String"));
            DataColumn colDpto = new DataColumn("Departamento", Type.GetType("System.String"));
            DataColumn colPCRBuscar = new DataColumn("PCRBuscar", Type.GetType("System.String"));
            DataColumn colPCRConfidencialBuscar = new DataColumn("PCRConfidencialBuscar", Type.GetType("System.String"));
            DataColumn colFondoIndependiente = new DataColumn("FondoIndependiente", Type.GetType("System.String"));
            DataColumn colNombreBuscar = new DataColumn("NombreBuscar", Type.GetType("System.String"));
            DataColumn colCargoBuscar = new DataColumn("CargoBuscar", Type.GetType("System.String"));
            DataColumn colDptoBuscar = new DataColumn("DepartamentoBuscar", Type.GetType("System.String"));
            DataColumn colCiudadDepartamento = new DataColumn("CiudadDepartamento", Type.GetType("System.String"));

            tblUsuarios.Columns.Add(colIdFirmanteBuscar);
            tblUsuarios.Columns.Add(colNombre);
            tblUsuarios.Columns.Add(colCargo);
            tblUsuarios.Columns.Add(colFirmanteBuscar);
            tblUsuarios.Columns.Add(colSiglaFirmanteBuscar);
            tblUsuarios.Columns.Add(colDpto);
            tblUsuarios.Columns.Add(colPCRBuscar);
            tblUsuarios.Columns.Add(colPCRConfidencialBuscar);
            tblUsuarios.Columns.Add(colFondoIndependiente);
            tblUsuarios.Columns.Add(colNombreBuscar);
            tblUsuarios.Columns.Add(colCargoBuscar);
            tblUsuarios.Columns.Add(colDptoBuscar);
            tblUsuarios.Columns.Add(colCiudadDepartamento);

            // Add five items.
            DataRow NewRow;
            int i;
            for (i = 0; i < Globals.ThisAddIn.listaEmpleados.Count; i++)
            {
                NewRow = tblUsuarios.NewRow();
                NewRow["Nombre"] = "Item " + i;
                tblUsuarios.Rows.Add(NewRow);
            }
            // Change the values in the table.
            tblUsuarios.AcceptChanges();
            ServicesMemorando.insertDataTable(tblUsuarios, Globals.ThisAddIn.listaEmpleados);
          
            
            // Create two DataView objects with the same table.
            vistaUsuarios = new DataView(tblUsuarios);
            dgvUsuarios.DataSource = vistaUsuarios;
            dgvUsuarios.Columns["Nombre"].Width = 218;
            dgvUsuarios.Columns["Cargo"].Width = 320;
            dgvUsuarios.Columns["Departamento"].Width = 360;
            dgvUsuarios.Columns["NombreBuscar"].Visible = false;
            dgvUsuarios.Columns["CargoBuscar"].Visible = false;
            dgvUsuarios.Columns["DepartamentoBuscar"].Visible = false;
            dgvUsuarios.Columns["PCRBuscar"].Visible = false;
            dgvUsuarios.Columns["PCRConfidencialBuscar"].Visible = false;
            dgvUsuarios.Columns["CiudadDepartamento"].Visible = false;
            dgvUsuarios.Columns["IdFirmanteBuscar"].Visible = false;
            dgvUsuarios.Columns["SiglaFirmanteBuscar"].Visible = false;
            dgvUsuarios.Columns["FirmanteBuscar"].Visible = false;
            dgvUsuarios.Columns["FondoIndependiente"].Visible = false;
        }


        /// <summary>
        /// validaciones que se realizan al cargar el formulario.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void SearchForm_Load(object sender, EventArgs e)
        {
            try
            {
                Globals.ThisAddIn.Application.ScreenUpdating = false;
                int iDestinos = Globals.ThisAddIn.listaDestinosMe.Count;
                if (!Globals.ThisAddIn.consultarValorPropiedad("00" + Constantes.propiedadPcrDestino).Equals("00" + Constantes.propiedadPcrDestino))
                {
                    primerDestAgregado = true;
                    btnActualizarPara.Enabled = true;
                    btnEliminarPara.Enabled = true;                   

                    ServicesMemorando.loadUsuarios();

                    if (iDestinos == Constantes.maxDestinatariosMemorando)
                    {
                        btInsertarPara.Enabled = false;
                    }
                }
                else
                {
                    Globals.ThisAddIn.listaDestinosMe.Clear();
                }

                if (!Globals.ThisAddIn.consultarValorPropiedad("00" + Constantes.propiedadPcrCopias).Equals("00" + Constantes.propiedadPcrCopias))
                {
                    primerCopiaAgregado = true;
                    btnActualizarCopia.Enabled = true;
                    btnEliminarCopia.Enabled = true;                    

                    ServicesMemorando.loadCopias();

                    if (Globals.ThisAddIn.listaCopiasMe.Count == Constantes.maxCopiasMemorando)
                    {
                        btnInsertarCopia.Enabled = false;
                    }
                }
                else
                {
                    Globals.ThisAddIn.listaCopiasMe.Clear();
                    chbCopias.Checked = false;
                    primerCopiaAgregado = false;                    
                }

                if (!Globals.ThisAddIn.consultarValorPropiedad("00" + Constantes.propiedadIdFirmante).Equals("00" + Constantes.propiedadIdFirmante))
                {
                    primerRemitenteAgregado = true;
                    ServicesCarta.loadFirmantes();
                    btnInsActDe.Enabled = false;
                }
                else
                {
                    Globals.ThisAddIn.listaFirmantes.Clear();
                    btnInsActDe.Enabled = true;
                }

                if (!Globals.ThisAddIn.consultarValorPropiedad("01" + Constantes.propiedadIdFirmante).Equals("01" + Constantes.propiedadIdFirmante))
                {
                    btnEliminarRemitente.Enabled = true;
                }
                if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadPrimerMarcaAgua).Equals("True"))
                {
                    primerMarcaAgregada = true;
                }
                if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadTipo).Equals("Confidencial"))
                {
                    rbtConfidencial.Checked = true;
                }

                if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadAnexosFisicos).Equals("False"))
                {
                    chbAFisico.Checked = false;
                }

                if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadAnexosElectronicos).Equals("True"))
                {
                    chbAElectronico.Checked = true;
                }

                /*
                 * Vamaya: Se añade este try para encontrar si la plantilla no tiene la propiedad añadida
                 */
                try
                {
                    /*
                     * Vamaya: Se añade esta validación para verificar el estado del chb Referencia
                     */
                    //Console.WriteLine("Impresión de prueba.");
                    //Globals.ThisAddIn.consultarTexto("00_NOMBRE_FIRMANTE");

                    if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadRef).Equals("False"))
                    {
                        chbReferencias.Checked = false;
                    }
                    /*
                     * Fin lineas de código insertadas
                     */
                }
                catch (Exception error)
                {
                    Globals.ThisAddIn.Application.ScreenUpdating = true;
                    Globals.ThisAddIn.escribirLog(error.ToString());
                    MessageBox.Show("Error, está usando una versión de plantilla desactualizada, por favor descargue la versión más reciente desde iConecta");
                    this.Close();
                }
                /*
                 * Fin lineas de código insertadas
                 */

                Globals.ThisAddIn.Application.ScreenUpdating = true;
            }
            catch (Exception error)
            {
                Globals.ThisAddIn.Application.ScreenUpdating = true;
                Globals.ThisAddIn.protegerArchivo();
                Globals.ThisAddIn.escribirLog(error.ToString());
                MessageBox.Show("Error al tratar de recuperar documento antiguo, Manipule el complemento desde un documento nuevo.");
                this.Close();
            }
        }

   
        private void btInsertarPara_Click(object sender, EventArgs e)
        {
            Globals.ThisAddIn.Application.ScreenUpdating = false;

            /*
             * Vamaya: Se añade esta validación para verificar el estado del los PCR's destino
             */
            if (Globals.ThisAddIn.validarDestinosMemorando() == false)
            {
                Globals.ThisAddIn.Application.ScreenUpdating = true;
                MessageBox.Show("Error: Problemas de caché en el Add-In, la lista de destinatarios está corrupta. Abra el Add-In nuevamente.");
                this.Close();
                //Se sale de la función para evitar validaciones adicionales depués de este paso
                return;
            }
            /*
             * Fin lineas de código insertadas
             */

            if (dgvUsuarios.SelectedRows.Count > 0)
            {
                if (primerDestAgregado)
                {
                    Globals.ThisAddIn.insertarDestinoMemorando();
                }
                else
                {
                    primerDestAgregado = true;
                    btnActualizarPara.Enabled = true;
                    btnEliminarPara.Enabled = true;
                }
                
                ServicesMemorando.insertarRegistro(dgvUsuarios, rbtConfidencial, txtNombre, btInsertarPara);
            }
            else
            {
                MessageBox.Show("Por favor seleccione una fila.");
            }
            Globals.ThisAddIn.Application.ScreenUpdating = true;
        }

        private void btnActualizarPara_Click(object sender, EventArgs e)
        {
            Globals.ThisAddIn.Application.ScreenUpdating = false;

            /*
             * Vamaya: Se añade esta validación para verificar el estado del los PCR's destino
             */
            if (Globals.ThisAddIn.validarDestinosMemorando() == false)
            {
                Globals.ThisAddIn.Application.ScreenUpdating = true;
                MessageBox.Show("Error: Problemas de caché en el Add-In, la lista de destinatarios está corrupta. Abra el Add-In nuevamente.");
                this.Close();
                //Se sale de la función para evitar validaciones adicionales depués de este paso
                return;
            }
            /*
             * Fin lineas de código insertadas
             */

            if (dgvUsuarios.SelectedRows.Count > 0)
            {
                Globals.ThisAddIn.listaDestinosMe.RemoveAt(Globals.ThisAddIn.listaDestinosMe.Count - 1);
                ServicesMemorando.insertarRegistro(dgvUsuarios, rbtConfidencial, txtNombre, btInsertarPara);
            }
            else
            {
                MessageBox.Show("Por favor seleccione una fila.");
            }
            Globals.ThisAddIn.Application.ScreenUpdating = true;
        }

        private void btnEliminarPara_Click(object sender, EventArgs e)
        {
            /*
             * Vamaya: Se añade esta validación para verificar el estado del los PCR's destino
             */
            if (Globals.ThisAddIn.validarDestinosMemorando() == false)
            {
                Globals.ThisAddIn.Application.ScreenUpdating = true;
                MessageBox.Show("Error: Problemas de caché en el Add-In, la lista de destinatarios está corrupta. Abra el Add-In nuevamente.");
                this.Close();
                //Se sale de la función para evitar validaciones adicionales depués de este paso
                return;
            }
            /*
             * Fin lineas de código insertadas
             */

            Globals.ThisAddIn.eliminarDestino(Globals.ThisAddIn.listaDestinosMe.Count);
            if(Globals.ThisAddIn.listaDestinosMe.Count > 1)
                Globals.ThisAddIn.listaDestinosMe.RemoveAt(Globals.ThisAddIn.listaDestinosMe.Count-1);
            btInsertarPara.Enabled = true;
        }

        private void dgvUsuarios_SelectionChanged(object sender, EventArgs e)
        {
            if (dgvUsuarios.SelectedRows.Count > 0)
            {

                string tipoFondo = Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadTipoFondo);

                if (tipoFondo != null)
                {
                    if (dgvUsuarios.CurrentRow.Cells["FirmanteBuscar"].Value.ToString().Equals("1") && dgvUsuarios.CurrentRow.Cells["SiglaFirmanteBuscar"].Value.ToString().Equals(tipoFondo))
                    {
                        btnInsActDe.Enabled = true;
                        btnActualizarRemitente.Enabled = true;
                    }
                    else
                    {
                        btnInsActDe.Enabled = false;
                        btnActualizarRemitente.Enabled = false;
                    }
                }
                else if (dgvUsuarios.CurrentRow.Cells["FirmanteBuscar"].Value.ToString().Equals("1"))
                {
                    btnInsActDe.Enabled = true;
                    btnActualizarRemitente.Enabled = true;
                }
                else
                {
                    btnInsActDe.Enabled = false;
                    btnActualizarRemitente.Enabled = false;
                }

                if (Globals.ThisAddIn.listaFirmantes.Count == Globals.ThisAddIn.iMaxRemitentes)
                {
                    btnInsActDe.Enabled = false;
                }

                if (primerRemitenteAgregado == false)
                {
                    btnEliminarRemitente.Enabled = false;
                    btnActualizarRemitente.Enabled = false;
                }
                string SegundoRemitente = Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadSegundoRemitente);
                if (SegundoRemitente.Equals("True"))
                {
                    btnInsActDe.Enabled = false;
                }

            }

        }

        private void btnInsActDe_Click(object sender, EventArgs e)
        {
            Globals.ThisAddIn.Application.ScreenUpdating = false;
            int firmantes;

            if (dgvUsuarios.SelectedRows.Count > 0)
            {
                if (primerRemitenteAgregado)
                {
                    primerMarcaAgregada = ServicesMemorando.manipularMarcaAgua(primerMarcaAgregada, rbtNormal);
                    Globals.ThisAddIn.insertarRemitente();
                    
                    btnEliminarRemitente.Enabled = true;
                    Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadSegundoRemitente, "True");
                }
                else
                {
                    primerRemitenteAgregado = true;
                }

                ServicesMemorando.actualizarRemitente(dgvUsuarios, Globals.ThisAddIn.listaFirmantes.Count);
                firmantes = Globals.ThisAddIn.listaFirmantes.Count - 1;

                if (firmantes == 0)
                {
                    if (dgvUsuarios.CurrentRow.Cells["FondoIndependiente"].FormattedValue.ToString() == "1")
                    {
                        Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadFondoIndependiente, "True");
                    }
                    else
                    {
                        Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadFondoIndependiente, "False");
                    }
                    
                }
                
                if(Globals.ThisAddIn.iMaxRemitentes == Globals.ThisAddIn.listaFirmantes.Count)
                    btnInsActDe.Enabled = false;
                
                LimpiarFormulario();
                txtNombre.Text = "";
                txtCargo.Text = "";
                txtDpto.Text = "";
                
            }
            else
            {
                MessageBox.Show("Por favor seleccione una fila");
            }
            Globals.ThisAddIn.Application.ScreenUpdating = true;
        }


        /// <summary>
        /// Limpia y restablece los valores por defecto de los elementos del formulario
        /// </summary>
        void LimpiarFormulario()
        {   
            txtNombre.Text = "";
            txtDpto.Text = "";
            txtCargo.Text = "";
            dgvUsuarios.ClearSelection();
        }


        private void btnActualizarRemitente_Click(object sender, EventArgs e)
        {
            Globals.ThisAddIn.Application.ScreenUpdating = false;
            if (dgvUsuarios.SelectedRows.Count > 0)
            {
                int iRemitente = Globals.ThisAddIn.listaFirmantes.Count - 1;
                Globals.ThisAddIn.listaFirmantes.RemoveAt(iRemitente);
                ServicesMemorando.actualizarRemitente(dgvUsuarios, iRemitente);

                if (iRemitente == 0)
                {
                    if (dgvUsuarios.CurrentRow.Cells["FondoIndependiente"].FormattedValue.ToString() == "1")
                    {
                        Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadFondoIndependiente, "True");
                    }
                    else
                    {
                        Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadFondoIndependiente, "False");
                    }
                }

                txtNombre.Text = "";
                txtCargo.Text = "";
                txtDpto.Text = "";
            }
            else
            {
                MessageBox.Show("Por favor seleccione una fila.");
            }
            Globals.ThisAddIn.Application.ScreenUpdating = true;
        }

        private void btnEliminarRemitente_Click(object sender, EventArgs e)

        {
            Globals.ThisAddIn.EliminarRemitente();
            btnEliminarRemitente.Enabled = false;
        }

        private void btnInsertarCopia_Click(object sender, EventArgs e)
        {
            bool bInsertar = true;

            if (dgvUsuarios.SelectedRows.Count > 0)
            {
                if (primerCopiaAgregado)
                {
                    Globals.ThisAddIn.insertarCopia(Globals.ThisAddIn.listaCopiasMe.Count);
                }
                else
                {
                    primerCopiaAgregado = true;
                    btnActualizarCopia.Enabled = true;
                    btnEliminarCopia.Enabled = true;
                }

                if (bInsertar)
                {
                  ServicesMemorando.insertarCopia(dgvUsuarios, rbtConfidencial, btnInsertarCopia, txtNombre);
                }
            }
            else
            {
                MessageBox.Show("Por favor seleccione una fila.");
            }
        }

        private void btnActualizarCopia_Click(object sender, EventArgs e)
        {
            if (dgvUsuarios.SelectedRows.Count > 0)
            {
                Globals.ThisAddIn.listaCopiasMe.RemoveAt(Globals.ThisAddIn.listaCopiasMe.Count - 1);
                ServicesMemorando.insertarCopia(dgvUsuarios, rbtConfidencial, btnInsertarCopia, txtNombre);
            }
            else
            {
                MessageBox.Show("Funcionario no encontrado en el lista. Favor verificar");
            }
        }

        private void btnEliminarCopia_Click(object sender, EventArgs e)
        {
            if (!Globals.ThisAddIn.eliminarCopia(Globals.ThisAddIn.listaCopiasMe.Count))
            {

                Globals.ThisAddIn.insertarTexto("00" + Constantes.tagCopias, "Nombre, Cargo, Dependencia (Copia). *" + DateTime.Now.Second.ToString(), false);
                primerCopiaAgregado = false;
                Globals.ThisAddIn.listaCopiasMe.RemoveAt(Globals.ThisAddIn.listaCopiasMe.Count - 1);
                btnActualizarCopia.Enabled = false;
                btnEliminarCopia.Enabled = false;

            }
            else
            {
                Globals.ThisAddIn.listaCopiasMe.RemoveAt(Globals.ThisAddIn.listaCopiasMe.Count - 1);
            }
            btnInsertarCopia.Enabled = true;

        }

        private void chbCopias_CheckStateChanged(object sender, EventArgs e)
        {
            Globals.ThisAddIn.Application.ScreenUpdating = false;
            if (chbCopias.Checked)
            {
                Globals.ThisAddIn.ajustarTexto(Constantes.tagTituloCopias, "Copias: *" + DateTime.Now.Second.ToString(), 12, WdColor.wdColorBlack);
                Globals.ThisAddIn.ajustarTexto("00" + Constantes.tagCopias, "Nombre, Cargo, Dependencia (Copia). *" + DateTime.Now.Second.ToString(), 12, WdColor.wdColorBlack);
                btnInsertarCopia.Enabled = true;

            }
            else
            {                
                ServicesMemorando.estadoCopias(btnInsertarCopia, btnActualizarCopia, btnEliminarCopia);
                Globals.ThisAddIn.listaCopiasMe.Clear();
                primerCopiaAgregado = false;

            }
            Globals.ThisAddIn.Application.ScreenUpdating = true;
        }

        private void rbtNormal_CheckedChanged(object sender, EventArgs e)
        {
            Globals.ThisAddIn.Application.ScreenUpdating = false;
            if (rbtNormal.Checked)
            {
                ServicesMemorando.actualizarRolPropiedad(Constantes.propiedadPcrDestino, Globals.ThisAddIn.listaDestinosMe, rbtNormal, rbtConfidencial, vistaUsuarios);
                if (chbCopias.Checked)
                {
                    ServicesMemorando.actualizarRolPropiedad(Constantes.propiedadPcrCopias, Globals.ThisAddIn.listaCopiasMe, rbtNormal, rbtConfidencial, vistaUsuarios);
                }
               Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorConfidencialPagPpal, "");
               Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorConfidencialOtrasPag, "");
                primerMarcaAgregada = ServicesMemorando.manipularMarcaAgua(primerMarcaAgregada, rbtNormal);
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadTipo, "Normal");
            }
            Globals.ThisAddIn.Application.ScreenUpdating = true;
        }

        private void rbtConfidencial_CheckedChanged(object sender, EventArgs e)
        {
            try
            {
                Globals.ThisAddIn.Application.ScreenUpdating = false;
                if (rbtConfidencial.Checked)
                {
                    ServicesMemorando.actualizarRolPropiedad(Constantes.propiedadPcrDestino, Globals.ThisAddIn.listaDestinosMe, rbtNormal, rbtConfidencial, vistaUsuarios);                                        
                    if (chbCopias.Checked)
                    {
                        ServicesMemorando.actualizarRolPropiedad(Constantes.propiedadPcrCopias, Globals.ThisAddIn.listaCopiasMe, rbtNormal, rbtConfidencial, vistaUsuarios);
                    }
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorConfidencialPagPpal, " CONFIDENCIAL");
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorConfidencialOtrasPag, " CONFIDENCIAL");
                    primerMarcaAgregada = ServicesMemorando.manipularMarcaAgua(primerMarcaAgregada, rbtNormal);
                    Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadTipo, "Confidencial");
                }
                Globals.ThisAddIn.Application.ScreenUpdating = true;
            }
            catch (Exception error)
            {
                Globals.ThisAddIn.Application.ScreenUpdating = true;
                Globals.ThisAddIn.protegerArchivo();
                Globals.ThisAddIn.escribirLog(error.ToString());
                MessageBox.Show("Error al agregar la tipología confidencial a un documento antiguo, Manipule el complemento desde un documento nuevo.");
                this.Close();
            }
        }

        
        private void chbAFisico_CheckedChanged(object sender, EventArgs e)
        {
            if (chbAFisico.Checked)
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadAnexosFisicos, "True");
                ServicesMemorando.editarMarcadorAnexos(chbAFisico, chbAElectronico);
            }
            else
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadAnexosFisicos, "False");
                ServicesMemorando.editarMarcadorAnexos(chbAFisico, chbAElectronico);
            }
        }

        private void chbAElectronico_CheckedChanged(object sender, EventArgs e)
        {
            if (chbAElectronico.Checked)
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadAnexosElectronicos, "True");
                ServicesMemorando.editarMarcadorAnexos(chbAFisico, chbAElectronico);
            }
            else
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadAnexosElectronicos, "False");
                ServicesMemorando.editarMarcadorAnexos(chbAFisico, chbAElectronico);
            }
        }

        private void chbReferencias_CheckedChanged(object sender, EventArgs e)
        {
            if (chbReferencias.Checked)
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadRef, "True"); //Vamaya: se cambia valor de la propiedad añadida
                Globals.ThisAddIn.ajustarTexto(String.Concat(Constantes.tagTituloReferencia), String.Concat(" ", "*"), 12, WdColor.wdColorBlack);
                Globals.ThisAddIn.ajustarTexto(String.Concat(Constantes.tagReferencia), String.Concat("Incluir referencia.", "*"), 12, WdColor.wdColorBlack);
            }
            else
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadRef, "False"); //Vamaya: se cambia valor de la propiedad añadida
                Globals.ThisAddIn.ajustarTexto(String.Concat(Constantes.tagTituloReferencia), String.Concat(" ", "*"), 1, WdColor.wdColorWhite);
                Globals.ThisAddIn.ajustarTexto(String.Concat(Constantes.tagReferencia), String.Concat(" ", "*"), 1, WdColor.wdColorWhite);
            }
        }

        private void txtNombre_TextChanged(object sender, EventArgs e)
        {
            Globals.ThisAddIn.buscarUsuarios(txtNombre.Text, txtCargo.Text, txtDpto.Text, vistaUsuarios);
        }

        private void txtCargo_TextChanged(object sender, EventArgs e)
        {
            Globals.ThisAddIn.buscarUsuarios(txtNombre.Text, txtCargo.Text, txtDpto.Text, vistaUsuarios);
        }

        private void txtDpto_TextChanged(object sender, EventArgs e)
        {
            Globals.ThisAddIn.buscarUsuarios(txtNombre.Text, txtCargo.Text, txtDpto.Text, vistaUsuarios);
        }

        private void btnLimpiar_Click(object sender, EventArgs e)
        {
            txtNombre.Text = "";
            txtCargo.Text = "";
            txtDpto.Text = "";
        }

        private void SearchForm_FormClosing(object sender, FormClosingEventArgs e)
        {
            Globals.ThisAddIn.iContVisitasForms--;
            Globals.ThisAddIn.Application.ScreenUpdating = true;

            //Vamaya: validacion de asunto vacío antes de cerrar addin
            //if (Globals.ThisAddIn.consultarTexto(Constantes.tagAsunto) == "Incluir asunto" || Globals.ThisAddIn.consultarTexto(Constantes.tagAsunto) == "" || Globals.ThisAddIn.consultarTexto(Constantes.tagAsunto) == " " || Globals.ThisAddIn.consultarTexto(Constantes.tagAsunto) == null)
            //{
             //   MessageBox.Show("Alerta: No ha insertado un asunto.");
            //}
            //Vamaya: fin

        }

        private void dgvUsuarios_CellContentClick(object sender, DataGridViewCellEventArgs e)
        {

        }

        /// <summary>
        /// Verifica la versión del AddIn, comparando la versión local con la especificada en el archivo plconf
        /// </summary>
        public void verificarAddIn()
        {
            if (Globals.ThisAddIn.ActAddin.Equals(Globals.ThisAddIn.VersThisAddin))
            {
                lblVersioAddInME.ForeColor = System.Drawing.Color.Green;
                lblVersioAddInME.Text = lblVersioAddInME.Text + Globals.ThisAddIn.VersThisAddin + " (Actualizado)";
            }
            else
            {
                lblVersioAddInME.ForeColor = System.Drawing.Color.Red;
                lblVersioAddInME.Text = lblVersioAddInME.Text + Globals.ThisAddIn.VersThisAddin + " (DESACTUALIZADO)";
            }
        }

        /// <summary>
        /// Verifica la versión local de la plantilla de memorando, comparándola con la especificada en el archivo plconf
        /// </summary>
        public void verificarPlantilla()
        {
            if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadVersionPlantilla).Equals(Globals.ThisAddIn.ActPlanME))
            {
                lblVersionPlantillaME.ForeColor = System.Drawing.Color.Green;
                lblVersionPlantillaME.Text = lblVersionPlantillaME.Text + Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadVersionPlantilla) + " (Actualizada)";
            }
            else
            {
                lblVersionPlantillaME.ForeColor = System.Drawing.Color.Red;
                lblVersionPlantillaME.Text = lblVersionPlantillaME.Text + Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadVersionPlantilla) + " (DESACTUALIZADA)";
            }
        }

        public void verificarAmbienteTrabajo()
        {
            Globals.ThisAddIn.verificarAmbiente();
        }
    }

}
