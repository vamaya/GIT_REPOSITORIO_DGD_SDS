namespace CorrWordAddIn
{
    partial class SearchFormCarta
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.btInsertarPara = new System.Windows.Forms.Button();
            this.dgvUsuarios = new System.Windows.Forms.DataGridView();
            this.txtNombre = new System.Windows.Forms.TextBox();
            this.lblNombre = new System.Windows.Forms.Label();
            this.txtCargo = new System.Windows.Forms.TextBox();
            this.lblCargo = new System.Windows.Forms.Label();
            this.txtDpto = new System.Windows.Forms.TextBox();
            this.lblDpto = new System.Windows.Forms.Label();
            this.gbxPara = new System.Windows.Forms.GroupBox();
            this.rbtCorreo = new System.Windows.Forms.RadioButton();
            this.rbtFisica = new System.Windows.Forms.RadioButton();
            this.cbCiudad = new System.Windows.Forms.ComboBox();
            this.cbPais = new System.Windows.Forms.ComboBox();
            this.lblPais = new System.Windows.Forms.Label();
            this.lblNombreExterno = new System.Windows.Forms.Label();
            this.TxtDireccion = new System.Windows.Forms.TextBox();
            this.lblCiudad = new System.Windows.Forms.Label();
            this.txtNombreexterno = new System.Windows.Forms.TextBox();
            this.lblCargoExterno = new System.Windows.Forms.Label();
            this.lblDireccion = new System.Windows.Forms.Label();
            this.txtCargoExterno = new System.Windows.Forms.TextBox();
            this.txtEmpresa = new System.Windows.Forms.TextBox();
            this.lblEntidad = new System.Windows.Forms.Label();
            this.btnVerificarDatos = new System.Windows.Forms.Button();
            this.btnEliminarPara = new System.Windows.Forms.Button();
            this.btnActualizarPara = new System.Windows.Forms.Button();
            this.btnInsertarRemitente = new System.Windows.Forms.Button();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.chbCopias = new System.Windows.Forms.CheckBox();
            this.btnEliminarCopia = new System.Windows.Forms.Button();
            this.btnActualizarCopia = new System.Windows.Forms.Button();
            this.btnInsertarCopia = new System.Windows.Forms.Button();
            this.chbAFisico = new System.Windows.Forms.CheckBox();
            this.chbAElectronico = new System.Windows.Forms.CheckBox();
            this.chbPersonalizado = new System.Windows.Forms.CheckBox();
            this.chbLogo = new System.Windows.Forms.CheckBox();
            this.gbxTipo = new System.Windows.Forms.GroupBox();
            this.rbtConfidencial = new System.Windows.Forms.RadioButton();
            this.rbtNormal = new System.Windows.Forms.RadioButton();
            this.groupBox3 = new System.Windows.Forms.GroupBox();
            this.groupBox4 = new System.Windows.Forms.GroupBox();
            this.chbReferencias = new System.Windows.Forms.CheckBox();
            this.chbIngles = new System.Windows.Forms.CheckBox();
            this.groupBox2 = new System.Windows.Forms.GroupBox();
            this.chbImpresionArea = new System.Windows.Forms.CheckBox();
            this.rbtMensajeria = new System.Windows.Forms.RadioButton();
            this.rbtDirecto = new System.Windows.Forms.RadioButton();
            this.groupBox5 = new System.Windows.Forms.GroupBox();
            this.btnEliminarRemitente = new System.Windows.Forms.Button();
            this.btnActualizarRemitente = new System.Windows.Forms.Button();
            this.btnLimpiar = new System.Windows.Forms.Button();
            this.groupBox6 = new System.Windows.Forms.GroupBox();
            this.rbtCorreoCertificado = new System.Windows.Forms.RadioButton();
            this.rbtCorreoNormal = new System.Windows.Forms.RadioButton();
            this.label1 = new System.Windows.Forms.Label();
            this.txtCopiaCorreo = new System.Windows.Forms.TextBox();
            this.groupBox7 = new System.Windows.Forms.GroupBox();
            this.groupBox8 = new System.Windows.Forms.GroupBox();
            this.lblVersionAddIn = new System.Windows.Forms.Label();
            this.lblVersionPlantillaCA = new System.Windows.Forms.Label();
            ((System.ComponentModel.ISupportInitialize)(this.dgvUsuarios)).BeginInit();
            this.gbxPara.SuspendLayout();
            this.groupBox1.SuspendLayout();
            this.gbxTipo.SuspendLayout();
            this.groupBox3.SuspendLayout();
            this.groupBox4.SuspendLayout();
            this.groupBox2.SuspendLayout();
            this.groupBox5.SuspendLayout();
            this.groupBox6.SuspendLayout();
            this.groupBox7.SuspendLayout();
            this.groupBox8.SuspendLayout();
            this.SuspendLayout();
            // 
            // btInsertarPara
            // 
            this.btInsertarPara.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btInsertarPara.Location = new System.Drawing.Point(6, 19);
            this.btInsertarPara.Name = "btInsertarPara";
            this.btInsertarPara.Size = new System.Drawing.Size(123, 23);
            this.btInsertarPara.TabIndex = 0;
            this.btInsertarPara.Text = "Insertar Destinatario";
            this.btInsertarPara.UseVisualStyleBackColor = true;
            this.btInsertarPara.Click += new System.EventHandler(this.btInsertarPara_Click);
            // 
            // dgvUsuarios
            // 
            this.dgvUsuarios.AllowUserToAddRows = false;
            this.dgvUsuarios.AllowUserToDeleteRows = false;
            this.dgvUsuarios.AllowUserToOrderColumns = true;
            this.dgvUsuarios.AllowUserToResizeColumns = false;
            this.dgvUsuarios.AllowUserToResizeRows = false;
            this.dgvUsuarios.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.DisableResizing;
            this.dgvUsuarios.Location = new System.Drawing.Point(8, 66);
            this.dgvUsuarios.Name = "dgvUsuarios";
            this.dgvUsuarios.ReadOnly = true;
            this.dgvUsuarios.Size = new System.Drawing.Size(720, 174);
            this.dgvUsuarios.TabIndex = 1;
            this.dgvUsuarios.RowEnter += new System.Windows.Forms.DataGridViewCellEventHandler(this.dgvUsuarios_RowEnter);
            this.dgvUsuarios.SelectionChanged += new System.EventHandler(this.dgvUsuarios_SelectionChanged);
            // 
            // txtNombre
            // 
            this.txtNombre.Location = new System.Drawing.Point(7, 40);
            this.txtNombre.Name = "txtNombre";
            this.txtNombre.Size = new System.Drawing.Size(205, 20);
            this.txtNombre.TabIndex = 2;
            this.txtNombre.TextChanged += new System.EventHandler(this.txtNombre_TextChanged_1);
            // 
            // lblNombre
            // 
            this.lblNombre.AutoSize = true;
            this.lblNombre.Location = new System.Drawing.Point(8, 22);
            this.lblNombre.Name = "lblNombre";
            this.lblNombre.Size = new System.Drawing.Size(47, 13);
            this.lblNombre.TabIndex = 4;
            this.lblNombre.Text = "Nombre:";
            // 
            // txtCargo
            // 
            this.txtCargo.Location = new System.Drawing.Point(227, 40);
            this.txtCargo.Name = "txtCargo";
            this.txtCargo.Size = new System.Drawing.Size(200, 20);
            this.txtCargo.TabIndex = 5;
            this.txtCargo.TextChanged += new System.EventHandler(this.txtCargo_TextChanged_1);
            // 
            // lblCargo
            // 
            this.lblCargo.AutoSize = true;
            this.lblCargo.Location = new System.Drawing.Point(227, 21);
            this.lblCargo.Name = "lblCargo";
            this.lblCargo.Size = new System.Drawing.Size(38, 13);
            this.lblCargo.TabIndex = 6;
            this.lblCargo.Text = "Cargo:";
            // 
            // txtDpto
            // 
            this.txtDpto.Location = new System.Drawing.Point(435, 40);
            this.txtDpto.Name = "txtDpto";
            this.txtDpto.Size = new System.Drawing.Size(205, 20);
            this.txtDpto.TabIndex = 7;
            this.txtDpto.TextChanged += new System.EventHandler(this.txtDpto_TextChanged_1);
            // 
            // lblDpto
            // 
            this.lblDpto.AutoSize = true;
            this.lblDpto.Location = new System.Drawing.Point(432, 22);
            this.lblDpto.Name = "lblDpto";
            this.lblDpto.Size = new System.Drawing.Size(71, 13);
            this.lblDpto.TabIndex = 8;
            this.lblDpto.Text = "Dependencia";
            // 
            // gbxPara
            // 
            this.gbxPara.Controls.Add(this.rbtCorreo);
            this.gbxPara.Controls.Add(this.rbtFisica);
            this.gbxPara.Controls.Add(this.cbCiudad);
            this.gbxPara.Controls.Add(this.cbPais);
            this.gbxPara.Controls.Add(this.lblPais);
            this.gbxPara.Controls.Add(this.lblNombreExterno);
            this.gbxPara.Controls.Add(this.TxtDireccion);
            this.gbxPara.Controls.Add(this.lblCiudad);
            this.gbxPara.Controls.Add(this.txtNombreexterno);
            this.gbxPara.Controls.Add(this.lblCargoExterno);
            this.gbxPara.Controls.Add(this.lblDireccion);
            this.gbxPara.Controls.Add(this.txtCargoExterno);
            this.gbxPara.Controls.Add(this.txtEmpresa);
            this.gbxPara.Controls.Add(this.lblEntidad);
            this.gbxPara.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.gbxPara.Location = new System.Drawing.Point(12, 72);
            this.gbxPara.Name = "gbxPara";
            this.gbxPara.Size = new System.Drawing.Size(260, 257);
            this.gbxPara.TabIndex = 9;
            this.gbxPara.TabStop = false;
            this.gbxPara.Text = "EXTERNOS:";
            // 
            // rbtCorreo
            // 
            this.rbtCorreo.AutoSize = true;
            this.rbtCorreo.Location = new System.Drawing.Point(136, 148);
            this.rbtCorreo.Name = "rbtCorreo";
            this.rbtCorreo.Size = new System.Drawing.Size(112, 17);
            this.rbtCorreo.TabIndex = 3;
            this.rbtCorreo.Text = "Correo Electrónico";
            this.rbtCorreo.UseVisualStyleBackColor = true;
            this.rbtCorreo.CheckedChanged += new System.EventHandler(this.rbtCorreo_CheckedChanged);
            // 
            // rbtFisica
            // 
            this.rbtFisica.AutoSize = true;
            this.rbtFisica.Checked = true;
            this.rbtFisica.Location = new System.Drawing.Point(72, 148);
            this.rbtFisica.Name = "rbtFisica";
            this.rbtFisica.Size = new System.Drawing.Size(54, 17);
            this.rbtFisica.TabIndex = 2;
            this.rbtFisica.TabStop = true;
            this.rbtFisica.Text = "Física";
            this.rbtFisica.UseVisualStyleBackColor = true;
            this.rbtFisica.CheckedChanged += new System.EventHandler(this.rbtFisica_CheckedChanged);
            // 
            // cbCiudad
            // 
            this.cbCiudad.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cbCiudad.DropDownWidth = 135;
            this.cbCiudad.FormattingEnabled = true;
            this.cbCiudad.Location = new System.Drawing.Point(131, 210);
            this.cbCiudad.Name = "cbCiudad";
            this.cbCiudad.Size = new System.Drawing.Size(115, 21);
            this.cbCiudad.TabIndex = 25;
            // 
            // cbPais
            // 
            this.cbPais.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cbPais.DropDownWidth = 135;
            this.cbPais.FormattingEnabled = true;
            this.cbPais.Location = new System.Drawing.Point(9, 210);
            this.cbPais.Name = "cbPais";
            this.cbPais.Size = new System.Drawing.Size(115, 21);
            this.cbPais.TabIndex = 24;
            this.cbPais.SelectedIndexChanged += new System.EventHandler(this.cbPais_SelectedIndexChanged);
            // 
            // lblPais
            // 
            this.lblPais.AutoSize = true;
            this.lblPais.Location = new System.Drawing.Point(10, 194);
            this.lblPais.Name = "lblPais";
            this.lblPais.Size = new System.Drawing.Size(32, 13);
            this.lblPais.TabIndex = 23;
            this.lblPais.Text = "País:";
            // 
            // lblNombreExterno
            // 
            this.lblNombreExterno.AutoSize = true;
            this.lblNombreExterno.Location = new System.Drawing.Point(10, 22);
            this.lblNombreExterno.Name = "lblNombreExterno";
            this.lblNombreExterno.Size = new System.Drawing.Size(47, 13);
            this.lblNombreExterno.TabIndex = 9;
            this.lblNombreExterno.Text = "Nombre:";
            this.lblNombreExterno.Click += new System.EventHandler(this.lblNombreExterno_Click);
            // 
            // TxtDireccion
            // 
            this.TxtDireccion.Location = new System.Drawing.Point(9, 171);
            this.TxtDireccion.Name = "TxtDireccion";
            this.TxtDireccion.Size = new System.Drawing.Size(237, 20);
            this.TxtDireccion.TabIndex = 20;
            // 
            // lblCiudad
            // 
            this.lblCiudad.AutoSize = true;
            this.lblCiudad.Location = new System.Drawing.Point(132, 194);
            this.lblCiudad.Name = "lblCiudad";
            this.lblCiudad.Size = new System.Drawing.Size(43, 13);
            this.lblCiudad.TabIndex = 21;
            this.lblCiudad.Text = "Ciudad:";
            // 
            // txtNombreexterno
            // 
            this.txtNombreexterno.Location = new System.Drawing.Point(9, 40);
            this.txtNombreexterno.Name = "txtNombreexterno";
            this.txtNombreexterno.Size = new System.Drawing.Size(237, 20);
            this.txtNombreexterno.TabIndex = 14;
            this.txtNombreexterno.TextChanged += new System.EventHandler(this.txtNombreexterno_TextChanged);
            // 
            // lblCargoExterno
            // 
            this.lblCargoExterno.AutoSize = true;
            this.lblCargoExterno.Location = new System.Drawing.Point(10, 63);
            this.lblCargoExterno.Name = "lblCargoExterno";
            this.lblCargoExterno.Size = new System.Drawing.Size(38, 13);
            this.lblCargoExterno.TabIndex = 15;
            this.lblCargoExterno.Text = "Cargo:";
            this.lblCargoExterno.Click += new System.EventHandler(this.lblCargoExterno_Click);
            // 
            // lblDireccion
            // 
            this.lblDireccion.AutoSize = true;
            this.lblDireccion.Location = new System.Drawing.Point(10, 150);
            this.lblDireccion.Name = "lblDireccion";
            this.lblDireccion.Size = new System.Drawing.Size(52, 13);
            this.lblDireccion.TabIndex = 19;
            this.lblDireccion.Text = "Dirección";
            // 
            // txtCargoExterno
            // 
            this.txtCargoExterno.Location = new System.Drawing.Point(9, 81);
            this.txtCargoExterno.Name = "txtCargoExterno";
            this.txtCargoExterno.Size = new System.Drawing.Size(237, 20);
            this.txtCargoExterno.TabIndex = 16;
            this.txtCargoExterno.TextChanged += new System.EventHandler(this.txtCargoExterno_TextChanged);
            // 
            // txtEmpresa
            // 
            this.txtEmpresa.Location = new System.Drawing.Point(9, 125);
            this.txtEmpresa.Name = "txtEmpresa";
            this.txtEmpresa.Size = new System.Drawing.Size(237, 20);
            this.txtEmpresa.TabIndex = 17;
            this.txtEmpresa.TextChanged += new System.EventHandler(this.txtEmpresa_TextChanged);
            // 
            // lblEntidad
            // 
            this.lblEntidad.AutoSize = true;
            this.lblEntidad.Location = new System.Drawing.Point(10, 104);
            this.lblEntidad.Name = "lblEntidad";
            this.lblEntidad.Size = new System.Drawing.Size(46, 13);
            this.lblEntidad.TabIndex = 18;
            this.lblEntidad.Text = "Entidad:";
            this.lblEntidad.Click += new System.EventHandler(this.lblEntidad_Click);
            // 
            // btnVerificarDatos
            // 
            this.btnVerificarDatos.Enabled = false;
            this.btnVerificarDatos.Location = new System.Drawing.Point(773, 353);
            this.btnVerificarDatos.Name = "btnVerificarDatos";
            this.btnVerificarDatos.Size = new System.Drawing.Size(214, 22);
            this.btnVerificarDatos.TabIndex = 23;
            this.btnVerificarDatos.Text = "Verificar Datos Destinatarios / Copias";
            this.btnVerificarDatos.UseVisualStyleBackColor = true;
            this.btnVerificarDatos.Click += new System.EventHandler(this.btnVerificarDatos_Click);
            // 
            // btnEliminarPara
            // 
            this.btnEliminarPara.Enabled = false;
            this.btnEliminarPara.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnEliminarPara.Location = new System.Drawing.Point(273, 19);
            this.btnEliminarPara.Name = "btnEliminarPara";
            this.btnEliminarPara.Size = new System.Drawing.Size(121, 23);
            this.btnEliminarPara.TabIndex = 2;
            this.btnEliminarPara.Text = "Eliminar Destinatario";
            this.btnEliminarPara.UseVisualStyleBackColor = true;
            this.btnEliminarPara.Click += new System.EventHandler(this.btnEliminarPara_Click);
            // 
            // btnActualizarPara
            // 
            this.btnActualizarPara.Enabled = false;
            this.btnActualizarPara.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnActualizarPara.Location = new System.Drawing.Point(135, 19);
            this.btnActualizarPara.Name = "btnActualizarPara";
            this.btnActualizarPara.Size = new System.Drawing.Size(131, 23);
            this.btnActualizarPara.TabIndex = 1;
            this.btnActualizarPara.Text = "Actualizar Destinatario";
            this.btnActualizarPara.UseVisualStyleBackColor = true;
            this.btnActualizarPara.Click += new System.EventHandler(this.btnActualizarPara_Click);
            // 
            // btnInsertarRemitente
            // 
            this.btnInsertarRemitente.Enabled = false;
            this.btnInsertarRemitente.Location = new System.Drawing.Point(6, 14);
            this.btnInsertarRemitente.Name = "btnInsertarRemitente";
            this.btnInsertarRemitente.Size = new System.Drawing.Size(123, 23);
            this.btnInsertarRemitente.TabIndex = 0;
            this.btnInsertarRemitente.Text = "Insertar Remitente";
            this.btnInsertarRemitente.UseVisualStyleBackColor = true;
            this.btnInsertarRemitente.Click += new System.EventHandler(this.btnInsRemitente_Click);
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add(this.btnEliminarPara);
            this.groupBox1.Controls.Add(this.btInsertarPara);
            this.groupBox1.Controls.Add(this.btnActualizarPara);
            this.groupBox1.Location = new System.Drawing.Point(12, 335);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(402, 49);
            this.groupBox1.TabIndex = 11;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "DESTINATARIOS";
            // 
            // chbCopias
            // 
            this.chbCopias.AutoSize = true;
            this.chbCopias.Checked = true;
            this.chbCopias.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chbCopias.Location = new System.Drawing.Point(57, 0);
            this.chbCopias.Name = "chbCopias";
            this.chbCopias.Size = new System.Drawing.Size(15, 14);
            this.chbCopias.TabIndex = 3;
            this.chbCopias.UseVisualStyleBackColor = true;
            this.chbCopias.CheckStateChanged += new System.EventHandler(this.chbCopias_CheckStateChanged);
            // 
            // btnEliminarCopia
            // 
            this.btnEliminarCopia.Enabled = false;
            this.btnEliminarCopia.Location = new System.Drawing.Point(212, 18);
            this.btnEliminarCopia.Name = "btnEliminarCopia";
            this.btnEliminarCopia.Size = new System.Drawing.Size(91, 23);
            this.btnEliminarCopia.TabIndex = 2;
            this.btnEliminarCopia.Text = "Eliminar Copia";
            this.btnEliminarCopia.UseVisualStyleBackColor = true;
            this.btnEliminarCopia.Click += new System.EventHandler(this.btnEliminarCopia_Click);
            // 
            // btnActualizarCopia
            // 
            this.btnActualizarCopia.Enabled = false;
            this.btnActualizarCopia.Location = new System.Drawing.Point(108, 19);
            this.btnActualizarCopia.Name = "btnActualizarCopia";
            this.btnActualizarCopia.Size = new System.Drawing.Size(100, 23);
            this.btnActualizarCopia.TabIndex = 1;
            this.btnActualizarCopia.Text = "Actualizar  Copia";
            this.btnActualizarCopia.UseVisualStyleBackColor = true;
            this.btnActualizarCopia.Click += new System.EventHandler(this.btnActualizarCopia_Click);
            // 
            // btnInsertarCopia
            // 
            this.btnInsertarCopia.Location = new System.Drawing.Point(6, 19);
            this.btnInsertarCopia.Name = "btnInsertarCopia";
            this.btnInsertarCopia.Size = new System.Drawing.Size(98, 23);
            this.btnInsertarCopia.TabIndex = 0;
            this.btnInsertarCopia.Text = "Insertar Copia";
            this.btnInsertarCopia.UseVisualStyleBackColor = true;
            this.btnInsertarCopia.Click += new System.EventHandler(this.btnInsertarCopia_Click);
            // 
            // chbAFisico
            // 
            this.chbAFisico.AutoSize = true;
            this.chbAFisico.Checked = true;
            this.chbAFisico.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chbAFisico.Location = new System.Drawing.Point(298, 15);
            this.chbAFisico.Name = "chbAFisico";
            this.chbAFisico.Size = new System.Drawing.Size(98, 17);
            this.chbAFisico.TabIndex = 3;
            this.chbAFisico.Text = "Anexos Físicos";
            this.chbAFisico.UseVisualStyleBackColor = true;
            this.chbAFisico.CheckedChanged += new System.EventHandler(this.chbAFisico_CheckedChanged);
            // 
            // chbAElectronico
            // 
            this.chbAElectronico.AutoSize = true;
            this.chbAElectronico.Location = new System.Drawing.Point(406, 15);
            this.chbAElectronico.Name = "chbAElectronico";
            this.chbAElectronico.Size = new System.Drawing.Size(122, 17);
            this.chbAElectronico.TabIndex = 2;
            this.chbAElectronico.Text = "Anexos Electrónicos";
            this.chbAElectronico.UseVisualStyleBackColor = true;
            this.chbAElectronico.CheckedChanged += new System.EventHandler(this.chbAElectronico_CheckedChanged);
            // 
            // chbPersonalizado
            // 
            this.chbPersonalizado.AutoSize = true;
            this.chbPersonalizado.Location = new System.Drawing.Point(126, 15);
            this.chbPersonalizado.Name = "chbPersonalizado";
            this.chbPersonalizado.Size = new System.Drawing.Size(162, 17);
            this.chbPersonalizado.TabIndex = 2;
            this.chbPersonalizado.Text = "Correspondencia Combinada";
            this.chbPersonalizado.UseVisualStyleBackColor = true;
            this.chbPersonalizado.CheckedChanged += new System.EventHandler(this.chbPersonalizado_CheckedChanged);
            // 
            // chbLogo
            // 
            this.chbLogo.AutoSize = true;
            this.chbLogo.Checked = true;
            this.chbLogo.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chbLogo.Location = new System.Drawing.Point(70, 15);
            this.chbLogo.Name = "chbLogo";
            this.chbLogo.Size = new System.Drawing.Size(50, 17);
            this.chbLogo.TabIndex = 1;
            this.chbLogo.Text = "Logo";
            this.chbLogo.UseVisualStyleBackColor = true;
            this.chbLogo.CheckedChanged += new System.EventHandler(this.chbLogo_CheckedChanged);
            // 
            // gbxTipo
            // 
            this.gbxTipo.Controls.Add(this.rbtConfidencial);
            this.gbxTipo.Controls.Add(this.rbtNormal);
            this.gbxTipo.Location = new System.Drawing.Point(12, 12);
            this.gbxTipo.Name = "gbxTipo";
            this.gbxTipo.Size = new System.Drawing.Size(248, 38);
            this.gbxTipo.TabIndex = 13;
            this.gbxTipo.TabStop = false;
            this.gbxTipo.Text = "TIPO DE CARTA:";
            this.gbxTipo.Enter += new System.EventHandler(this.gbxTipo_Enter);
            // 
            // rbtConfidencial
            // 
            this.rbtConfidencial.AutoSize = true;
            this.rbtConfidencial.Location = new System.Drawing.Point(86, 16);
            this.rbtConfidencial.Name = "rbtConfidencial";
            this.rbtConfidencial.Size = new System.Drawing.Size(83, 17);
            this.rbtConfidencial.TabIndex = 1;
            this.rbtConfidencial.Text = "Confidencial";
            this.rbtConfidencial.UseVisualStyleBackColor = true;
            this.rbtConfidencial.CheckedChanged += new System.EventHandler(this.rbtConfidencial_CheckedChanged);
            // 
            // rbtNormal
            // 
            this.rbtNormal.AutoSize = true;
            this.rbtNormal.Checked = true;
            this.rbtNormal.Location = new System.Drawing.Point(10, 16);
            this.rbtNormal.Name = "rbtNormal";
            this.rbtNormal.Size = new System.Drawing.Size(58, 17);
            this.rbtNormal.TabIndex = 0;
            this.rbtNormal.TabStop = true;
            this.rbtNormal.Text = "Normal";
            this.rbtNormal.UseVisualStyleBackColor = true;
            this.rbtNormal.CheckedChanged += new System.EventHandler(this.rbtNormal_CheckedChanged);
            // 
            // groupBox3
            // 
            this.groupBox3.Controls.Add(this.chbCopias);
            this.groupBox3.Controls.Add(this.btnEliminarCopia);
            this.groupBox3.Controls.Add(this.btnInsertarCopia);
            this.groupBox3.Controls.Add(this.btnActualizarCopia);
            this.groupBox3.Location = new System.Drawing.Point(437, 335);
            this.groupBox3.Name = "groupBox3";
            this.groupBox3.Size = new System.Drawing.Size(312, 49);
            this.groupBox3.TabIndex = 12;
            this.groupBox3.TabStop = false;
            this.groupBox3.Text = "COPIAS";
            // 
            // groupBox4
            // 
            this.groupBox4.Controls.Add(this.chbReferencias);
            this.groupBox4.Controls.Add(this.chbAElectronico);
            this.groupBox4.Controls.Add(this.chbAFisico);
            this.groupBox4.Controls.Add(this.chbIngles);
            this.groupBox4.Controls.Add(this.chbLogo);
            this.groupBox4.Controls.Add(this.chbPersonalizado);
            this.groupBox4.Location = new System.Drawing.Point(278, 12);
            this.groupBox4.Name = "groupBox4";
            this.groupBox4.Size = new System.Drawing.Size(621, 38);
            this.groupBox4.TabIndex = 15;
            this.groupBox4.TabStop = false;
            this.groupBox4.Text = "DETALLES";
            this.groupBox4.Enter += new System.EventHandler(this.groupBox4_Enter);
            // 
            // chbReferencias
            // 
            this.chbReferencias.AutoSize = true;
            this.chbReferencias.Checked = true;
            this.chbReferencias.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chbReferencias.Location = new System.Drawing.Point(537, 15);
            this.chbReferencias.Name = "chbReferencias";
            this.chbReferencias.Size = new System.Drawing.Size(78, 17);
            this.chbReferencias.TabIndex = 4;
            this.chbReferencias.Text = "Referencia";
            this.chbReferencias.UseVisualStyleBackColor = true;
            this.chbReferencias.CheckedChanged += new System.EventHandler(this.chbReferencias_CheckedChanged);
            // 
            // chbIngles
            // 
            this.chbIngles.AutoSize = true;
            this.chbIngles.Location = new System.Drawing.Point(10, 15);
            this.chbIngles.Name = "chbIngles";
            this.chbIngles.Size = new System.Drawing.Size(54, 17);
            this.chbIngles.TabIndex = 3;
            this.chbIngles.Text = "Inglés";
            this.chbIngles.UseVisualStyleBackColor = true;
            this.chbIngles.CheckedChanged += new System.EventHandler(this.chbIngles_CheckedChanged);
            // 
            // groupBox2
            // 
            this.groupBox2.Controls.Add(this.chbImpresionArea);
            this.groupBox2.Controls.Add(this.rbtMensajeria);
            this.groupBox2.Controls.Add(this.rbtDirecto);
            this.groupBox2.Location = new System.Drawing.Point(21, 19);
            this.groupBox2.Name = "groupBox2";
            this.groupBox2.Size = new System.Drawing.Size(393, 42);
            this.groupBox2.TabIndex = 24;
            this.groupBox2.TabStop = false;
            this.groupBox2.Text = "ENVÍO FÍSICO";
            // 
            // chbImpresionArea
            // 
            this.chbImpresionArea.AutoSize = true;
            this.chbImpresionArea.Location = new System.Drawing.Point(256, 18);
            this.chbImpresionArea.Name = "chbImpresionArea";
            this.chbImpresionArea.Size = new System.Drawing.Size(122, 17);
            this.chbImpresionArea.TabIndex = 5;
            this.chbImpresionArea.Text = "Impresión en el Área";
            this.chbImpresionArea.UseVisualStyleBackColor = true;
            this.chbImpresionArea.CheckedChanged += new System.EventHandler(this.chbImpresionArea_CheckedChanged);
            // 
            // rbtMensajeria
            // 
            this.rbtMensajeria.AutoSize = true;
            this.rbtMensajeria.Checked = true;
            this.rbtMensajeria.Location = new System.Drawing.Point(6, 17);
            this.rbtMensajeria.Name = "rbtMensajeria";
            this.rbtMensajeria.Size = new System.Drawing.Size(119, 17);
            this.rbtMensajeria.TabIndex = 1;
            this.rbtMensajeria.TabStop = true;
            this.rbtMensajeria.Text = "Servicio Mensajería";
            this.rbtMensajeria.UseVisualStyleBackColor = true;
            this.rbtMensajeria.CheckedChanged += new System.EventHandler(this.rbtMensajeria_CheckedChanged);
            // 
            // rbtDirecto
            // 
            this.rbtDirecto.AutoSize = true;
            this.rbtDirecto.Location = new System.Drawing.Point(136, 17);
            this.rbtDirecto.Name = "rbtDirecto";
            this.rbtDirecto.Size = new System.Drawing.Size(109, 17);
            this.rbtDirecto.TabIndex = 0;
            this.rbtDirecto.Text = "Directo al Destino";
            this.rbtDirecto.UseVisualStyleBackColor = true;
            this.rbtDirecto.CheckedChanged += new System.EventHandler(this.rbtDirecto_CheckedChanged);
            // 
            // groupBox5
            // 
            this.groupBox5.Controls.Add(this.btnEliminarRemitente);
            this.groupBox5.Controls.Add(this.btnActualizarRemitente);
            this.groupBox5.Controls.Add(this.btnInsertarRemitente);
            this.groupBox5.Location = new System.Drawing.Point(12, 407);
            this.groupBox5.Name = "groupBox5";
            this.groupBox5.Size = new System.Drawing.Size(402, 42);
            this.groupBox5.TabIndex = 25;
            this.groupBox5.TabStop = false;
            this.groupBox5.Text = "REMITENTES";
            // 
            // btnEliminarRemitente
            // 
            this.btnEliminarRemitente.Enabled = false;
            this.btnEliminarRemitente.Location = new System.Drawing.Point(273, 13);
            this.btnEliminarRemitente.Name = "btnEliminarRemitente";
            this.btnEliminarRemitente.Size = new System.Drawing.Size(121, 23);
            this.btnEliminarRemitente.TabIndex = 2;
            this.btnEliminarRemitente.Text = "Eliminar Remitente";
            this.btnEliminarRemitente.UseVisualStyleBackColor = true;
            this.btnEliminarRemitente.Click += new System.EventHandler(this.btnEliminarRemitente_Click);
            // 
            // btnActualizarRemitente
            // 
            this.btnActualizarRemitente.Enabled = false;
            this.btnActualizarRemitente.Location = new System.Drawing.Point(135, 14);
            this.btnActualizarRemitente.Name = "btnActualizarRemitente";
            this.btnActualizarRemitente.Size = new System.Drawing.Size(131, 23);
            this.btnActualizarRemitente.TabIndex = 1;
            this.btnActualizarRemitente.Text = "Actualizar Remitente";
            this.btnActualizarRemitente.UseVisualStyleBackColor = true;
            this.btnActualizarRemitente.Click += new System.EventHandler(this.btnActualizarRemitente_Click);
            // 
            // btnLimpiar
            // 
            this.btnLimpiar.Location = new System.Drawing.Point(652, 37);
            this.btnLimpiar.Name = "btnLimpiar";
            this.btnLimpiar.Size = new System.Drawing.Size(75, 23);
            this.btnLimpiar.TabIndex = 26;
            this.btnLimpiar.Text = "Limpiar";
            this.btnLimpiar.UseVisualStyleBackColor = true;
            this.btnLimpiar.Click += new System.EventHandler(this.btnLimpiar_Click);
            // 
            // groupBox6
            // 
            this.groupBox6.Controls.Add(this.rbtCorreoCertificado);
            this.groupBox6.Controls.Add(this.rbtCorreoNormal);
            this.groupBox6.Controls.Add(this.label1);
            this.groupBox6.Controls.Add(this.txtCopiaCorreo);
            this.groupBox6.Location = new System.Drawing.Point(433, 19);
            this.groupBox6.Name = "groupBox6";
            this.groupBox6.Size = new System.Drawing.Size(398, 42);
            this.groupBox6.TabIndex = 27;
            this.groupBox6.TabStop = false;
            this.groupBox6.Text = "ENVÍO POR CORREO ELECTRÓNICO";
            // 
            // rbtCorreoCertificado
            // 
            this.rbtCorreoCertificado.AutoSize = true;
            this.rbtCorreoCertificado.Location = new System.Drawing.Point(264, -2);
            this.rbtCorreoCertificado.Name = "rbtCorreoCertificado";
            this.rbtCorreoCertificado.Size = new System.Drawing.Size(75, 17);
            this.rbtCorreoCertificado.TabIndex = 3;
            this.rbtCorreoCertificado.Text = "Certificado";
            this.rbtCorreoCertificado.UseVisualStyleBackColor = true;
            this.rbtCorreoCertificado.CheckedChanged += new System.EventHandler(this.rbtCorreoCertificado_CheckedChanged);
            // 
            // rbtCorreoNormal
            // 
            this.rbtCorreoNormal.AutoSize = true;
            this.rbtCorreoNormal.Checked = true;
            this.rbtCorreoNormal.Location = new System.Drawing.Point(201, -2);
            this.rbtCorreoNormal.Name = "rbtCorreoNormal";
            this.rbtCorreoNormal.Size = new System.Drawing.Size(58, 17);
            this.rbtCorreoNormal.TabIndex = 2;
            this.rbtCorreoNormal.TabStop = true;
            this.rbtCorreoNormal.Text = "Normal";
            this.rbtCorreoNormal.UseVisualStyleBackColor = true;
            this.rbtCorreoNormal.CheckedChanged += new System.EventHandler(this.rbtCorreoNormal_CheckedChanged);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(6, 21);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(136, 13);
            this.label1.TabIndex = 26;
            this.label1.Text = "Copia a Buzón Corporativo:";
            // 
            // txtCopiaCorreo
            // 
            this.txtCopiaCorreo.Location = new System.Drawing.Point(154, 17);
            this.txtCopiaCorreo.Name = "txtCopiaCorreo";
            this.txtCopiaCorreo.Size = new System.Drawing.Size(241, 20);
            this.txtCopiaCorreo.TabIndex = 27;
            this.txtCopiaCorreo.Leave += new System.EventHandler(this.txtCopiaCorreo_Leave);
            // 
            // groupBox7
            // 
            this.groupBox7.Controls.Add(this.lblDpto);
            this.groupBox7.Controls.Add(this.txtNombre);
            this.groupBox7.Controls.Add(this.btnLimpiar);
            this.groupBox7.Controls.Add(this.lblCargo);
            this.groupBox7.Controls.Add(this.lblNombre);
            this.groupBox7.Controls.Add(this.txtDpto);
            this.groupBox7.Controls.Add(this.txtCargo);
            this.groupBox7.Controls.Add(this.dgvUsuarios);
            this.groupBox7.Location = new System.Drawing.Point(278, 72);
            this.groupBox7.Name = "groupBox7";
            this.groupBox7.Size = new System.Drawing.Size(733, 257);
            this.groupBox7.TabIndex = 28;
            this.groupBox7.TabStop = false;
            this.groupBox7.Text = "INTERNOS:";
            // 
            // groupBox8
            // 
            this.groupBox8.Controls.Add(this.groupBox6);
            this.groupBox8.Controls.Add(this.groupBox2);
            this.groupBox8.Location = new System.Drawing.Point(12, 464);
            this.groupBox8.Name = "groupBox8";
            this.groupBox8.Size = new System.Drawing.Size(846, 74);
            this.groupBox8.TabIndex = 29;
            this.groupBox8.TabStop = false;
            this.groupBox8.Text = "OPCIONES DE ENVÍO";
            // 
            // lblVersionAddIn
            // 
            this.lblVersionAddIn.AutoSize = true;
            this.lblVersionAddIn.Location = new System.Drawing.Point(777, 407);
            this.lblVersionAddIn.Name = "lblVersionAddIn";
            this.lblVersionAddIn.Size = new System.Drawing.Size(76, 13);
            this.lblVersionAddIn.TabIndex = 30;
            this.lblVersionAddIn.Text = "Versión AddIn:";
            // 
            // lblVersionPlantillaCA
            // 
            this.lblVersionPlantillaCA.AutoSize = true;
            this.lblVersionPlantillaCA.Location = new System.Drawing.Point(777, 436);
            this.lblVersionPlantillaCA.Name = "lblVersionPlantillaCA";
            this.lblVersionPlantillaCA.Size = new System.Drawing.Size(84, 13);
            this.lblVersionPlantillaCA.TabIndex = 31;
            this.lblVersionPlantillaCA.Text = "Versión Plantilla:";
            // 
            // SearchFormCarta
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(1015, 538);
            this.Controls.Add(this.lblVersionPlantillaCA);
            this.Controls.Add(this.lblVersionAddIn);
            this.Controls.Add(this.groupBox8);
            this.Controls.Add(this.groupBox7);
            this.Controls.Add(this.groupBox5);
            this.Controls.Add(this.btnVerificarDatos);
            this.Controls.Add(this.groupBox4);
            this.Controls.Add(this.groupBox3);
            this.Controls.Add(this.gbxTipo);
            this.Controls.Add(this.groupBox1);
            this.Controls.Add(this.gbxPara);
            this.Name = "SearchFormCarta";
            this.Text = "Módulo de Carta";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.SearchFormCarta_FormClosing);
            this.Load += new System.EventHandler(this.SearchFormCarta_Load);
            ((System.ComponentModel.ISupportInitialize)(this.dgvUsuarios)).EndInit();
            this.gbxPara.ResumeLayout(false);
            this.gbxPara.PerformLayout();
            this.groupBox1.ResumeLayout(false);
            this.gbxTipo.ResumeLayout(false);
            this.gbxTipo.PerformLayout();
            this.groupBox3.ResumeLayout(false);
            this.groupBox3.PerformLayout();
            this.groupBox4.ResumeLayout(false);
            this.groupBox4.PerformLayout();
            this.groupBox2.ResumeLayout(false);
            this.groupBox2.PerformLayout();
            this.groupBox5.ResumeLayout(false);
            this.groupBox6.ResumeLayout(false);
            this.groupBox6.PerformLayout();
            this.groupBox7.ResumeLayout(false);
            this.groupBox7.PerformLayout();
            this.groupBox8.ResumeLayout(false);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button btInsertarPara;
        public System.Windows.Forms.DataGridView dgvUsuarios;
        private System.Windows.Forms.TextBox txtNombre;
        private System.Windows.Forms.Label lblNombre;
        private System.Windows.Forms.TextBox txtCargo;
        private System.Windows.Forms.Label lblCargo;
        private System.Windows.Forms.TextBox txtDpto;
        private System.Windows.Forms.Label lblDpto;
        private System.Windows.Forms.GroupBox gbxPara;
        private System.Windows.Forms.Button btnActualizarPara;
        private System.Windows.Forms.Button btnEliminarPara;
        private System.Windows.Forms.Button btnInsertarRemitente;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.CheckBox chbCopias;
        private System.Windows.Forms.Button btnEliminarCopia;
        private System.Windows.Forms.Button btnActualizarCopia;
        private System.Windows.Forms.Button btnInsertarCopia;
        private System.Windows.Forms.GroupBox gbxTipo;
        private System.Windows.Forms.RadioButton rbtConfidencial;
        private System.Windows.Forms.RadioButton rbtNormal;
        private System.Windows.Forms.Label lblNombreExterno;
        private System.Windows.Forms.TextBox txtNombreexterno;
        private System.Windows.Forms.Label lblCargoExterno;
        private System.Windows.Forms.TextBox txtCargoExterno;
        private System.Windows.Forms.TextBox txtEmpresa;
        private System.Windows.Forms.Label lblEntidad;
        private System.Windows.Forms.Label lblDireccion;
        private System.Windows.Forms.Label lblCiudad;
        private System.Windows.Forms.TextBox TxtDireccion;
        private System.Windows.Forms.CheckBox chbLogo;
        private System.Windows.Forms.GroupBox groupBox3;
        private System.Windows.Forms.CheckBox chbPersonalizado;
        private System.Windows.Forms.GroupBox groupBox4;
        private System.Windows.Forms.CheckBox chbIngles;
        private System.Windows.Forms.CheckBox chbAElectronico;
        private System.Windows.Forms.CheckBox chbAFisico;
        private System.Windows.Forms.CheckBox chbReferencias;
        private System.Windows.Forms.Label lblPais;
        private System.Windows.Forms.ComboBox cbPais;
        private System.Windows.Forms.ComboBox cbCiudad;
        private System.Windows.Forms.Button btnVerificarDatos;
        private System.Windows.Forms.GroupBox groupBox2;
        private System.Windows.Forms.RadioButton rbtMensajeria;
        private System.Windows.Forms.RadioButton rbtDirecto;
        private System.Windows.Forms.GroupBox groupBox5;
        private System.Windows.Forms.Button btnLimpiar;
        private System.Windows.Forms.Button btnEliminarRemitente;
        private System.Windows.Forms.Button btnActualizarRemitente;
        private System.Windows.Forms.RadioButton rbtCorreo;
        private System.Windows.Forms.RadioButton rbtFisica;
        private System.Windows.Forms.CheckBox chbImpresionArea;
        private System.Windows.Forms.GroupBox groupBox6;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.TextBox txtCopiaCorreo;
        private System.Windows.Forms.RadioButton rbtCorreoCertificado;
        private System.Windows.Forms.RadioButton rbtCorreoNormal;
        private System.Windows.Forms.GroupBox groupBox7;
        private System.Windows.Forms.GroupBox groupBox8;
        private System.Windows.Forms.Label lblVersionAddIn;
        private System.Windows.Forms.Label lblVersionPlantillaCA;
    }
}