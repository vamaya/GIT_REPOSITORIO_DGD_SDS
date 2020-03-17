namespace CorrWordAddIn
{
    partial class SearchForm
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
            this.btnLimpiar = new System.Windows.Forms.Button();
            this.lblNombre = new System.Windows.Forms.Label();
            this.txtCargo = new System.Windows.Forms.TextBox();
            this.lblCargo = new System.Windows.Forms.Label();
            this.txtDpto = new System.Windows.Forms.TextBox();
            this.lblDpto = new System.Windows.Forms.Label();
            this.gbxPara = new System.Windows.Forms.GroupBox();
            this.btnEliminarPara = new System.Windows.Forms.Button();
            this.btnActualizarPara = new System.Windows.Forms.Button();
            this.gbxDe = new System.Windows.Forms.GroupBox();
            this.btnActualizarRemitente = new System.Windows.Forms.Button();
            this.btnEliminarRemitente = new System.Windows.Forms.Button();
            this.btnInsActDe = new System.Windows.Forms.Button();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.chbCopias = new System.Windows.Forms.CheckBox();
            this.btnEliminarCopia = new System.Windows.Forms.Button();
            this.btnActualizarCopia = new System.Windows.Forms.Button();
            this.btnInsertarCopia = new System.Windows.Forms.Button();
            this.groupBox2 = new System.Windows.Forms.GroupBox();
            this.chbReferencias = new System.Windows.Forms.CheckBox();
            this.chbAFisico = new System.Windows.Forms.CheckBox();
            this.chbAElectronico = new System.Windows.Forms.CheckBox();
            this.gbxTipo = new System.Windows.Forms.GroupBox();
            this.rbtConfidencial = new System.Windows.Forms.RadioButton();
            this.rbtNormal = new System.Windows.Forms.RadioButton();
            this.lblVersioAddInME = new System.Windows.Forms.Label();
            this.lblVersionPlantillaME = new System.Windows.Forms.Label();
            ((System.ComponentModel.ISupportInitialize)(this.dgvUsuarios)).BeginInit();
            this.gbxPara.SuspendLayout();
            this.gbxDe.SuspendLayout();
            this.groupBox1.SuspendLayout();
            this.groupBox2.SuspendLayout();
            this.gbxTipo.SuspendLayout();
            this.SuspendLayout();
            // 
            // btInsertarPara
            // 
            this.btInsertarPara.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btInsertarPara.Location = new System.Drawing.Point(7, 21);
            this.btInsertarPara.Name = "btInsertarPara";
            this.btInsertarPara.Size = new System.Drawing.Size(119, 23);
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
            this.dgvUsuarios.Location = new System.Drawing.Point(12, 101);
            this.dgvUsuarios.Name = "dgvUsuarios";
            this.dgvUsuarios.ReadOnly = true;
            this.dgvUsuarios.Size = new System.Drawing.Size(959, 332);
            this.dgvUsuarios.TabIndex = 1;
            this.dgvUsuarios.CellContentClick += new System.Windows.Forms.DataGridViewCellEventHandler(this.dgvUsuarios_CellContentClick);
            this.dgvUsuarios.SelectionChanged += new System.EventHandler(this.dgvUsuarios_SelectionChanged);
            // 
            // txtNombre
            // 
            this.txtNombre.Location = new System.Drawing.Point(12, 75);
            this.txtNombre.Name = "txtNombre";
            this.txtNombre.Size = new System.Drawing.Size(280, 20);
            this.txtNombre.TabIndex = 2;
            this.txtNombre.TextChanged += new System.EventHandler(this.txtNombre_TextChanged);
            // 
            // btnLimpiar
            // 
            this.btnLimpiar.Location = new System.Drawing.Point(896, 71);
            this.btnLimpiar.Name = "btnLimpiar";
            this.btnLimpiar.Size = new System.Drawing.Size(75, 23);
            this.btnLimpiar.TabIndex = 3;
            this.btnLimpiar.Text = "Limpiar";
            this.btnLimpiar.UseVisualStyleBackColor = true;
            this.btnLimpiar.Click += new System.EventHandler(this.btnLimpiar_Click);
            // 
            // lblNombre
            // 
            this.lblNombre.AutoSize = true;
            this.lblNombre.Location = new System.Drawing.Point(13, 57);
            this.lblNombre.Name = "lblNombre";
            this.lblNombre.Size = new System.Drawing.Size(47, 13);
            this.lblNombre.TabIndex = 4;
            this.lblNombre.Text = "Nombre:";
            // 
            // txtCargo
            // 
            this.txtCargo.Location = new System.Drawing.Point(308, 75);
            this.txtCargo.Name = "txtCargo";
            this.txtCargo.Size = new System.Drawing.Size(280, 20);
            this.txtCargo.TabIndex = 5;
            this.txtCargo.TextChanged += new System.EventHandler(this.txtCargo_TextChanged);
            // 
            // lblCargo
            // 
            this.lblCargo.AutoSize = true;
            this.lblCargo.Location = new System.Drawing.Point(305, 57);
            this.lblCargo.Name = "lblCargo";
            this.lblCargo.Size = new System.Drawing.Size(38, 13);
            this.lblCargo.TabIndex = 6;
            this.lblCargo.Text = "Cargo:";
            // 
            // txtDpto
            // 
            this.txtDpto.Location = new System.Drawing.Point(606, 74);
            this.txtDpto.Name = "txtDpto";
            this.txtDpto.Size = new System.Drawing.Size(280, 20);
            this.txtDpto.TabIndex = 7;
            this.txtDpto.TextChanged += new System.EventHandler(this.txtDpto_TextChanged);
            // 
            // lblDpto
            // 
            this.lblDpto.AutoSize = true;
            this.lblDpto.Location = new System.Drawing.Point(603, 57);
            this.lblDpto.Name = "lblDpto";
            this.lblDpto.Size = new System.Drawing.Size(71, 13);
            this.lblDpto.TabIndex = 8;
            this.lblDpto.Text = "Dependencia";
            // 
            // gbxPara
            // 
            this.gbxPara.Controls.Add(this.btnEliminarPara);
            this.gbxPara.Controls.Add(this.btnActualizarPara);
            this.gbxPara.Controls.Add(this.btInsertarPara);
            this.gbxPara.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.gbxPara.Location = new System.Drawing.Point(11, 439);
            this.gbxPara.Name = "gbxPara";
            this.gbxPara.Size = new System.Drawing.Size(423, 52);
            this.gbxPara.TabIndex = 9;
            this.gbxPara.TabStop = false;
            this.gbxPara.Text = "DESTINATARIOS:";
            // 
            // btnEliminarPara
            // 
            this.btnEliminarPara.Enabled = false;
            this.btnEliminarPara.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnEliminarPara.Location = new System.Drawing.Point(277, 21);
            this.btnEliminarPara.Name = "btnEliminarPara";
            this.btnEliminarPara.Size = new System.Drawing.Size(133, 23);
            this.btnEliminarPara.TabIndex = 2;
            this.btnEliminarPara.Text = "Eliminar Destinatario";
            this.btnEliminarPara.UseVisualStyleBackColor = true;
            this.btnEliminarPara.Click += new System.EventHandler(this.btnEliminarPara_Click);
            // 
            // btnActualizarPara
            // 
            this.btnActualizarPara.Enabled = false;
            this.btnActualizarPara.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.btnActualizarPara.Location = new System.Drawing.Point(137, 21);
            this.btnActualizarPara.Name = "btnActualizarPara";
            this.btnActualizarPara.Size = new System.Drawing.Size(131, 23);
            this.btnActualizarPara.TabIndex = 1;
            this.btnActualizarPara.Text = "Actualizar Destinatario";
            this.btnActualizarPara.UseVisualStyleBackColor = true;
            this.btnActualizarPara.Click += new System.EventHandler(this.btnActualizarPara_Click);
            // 
            // gbxDe
            // 
            this.gbxDe.Controls.Add(this.btnActualizarRemitente);
            this.gbxDe.Controls.Add(this.btnEliminarRemitente);
            this.gbxDe.Controls.Add(this.btnInsActDe);
            this.gbxDe.Location = new System.Drawing.Point(12, 497);
            this.gbxDe.Name = "gbxDe";
            this.gbxDe.Size = new System.Drawing.Size(425, 52);
            this.gbxDe.TabIndex = 10;
            this.gbxDe.TabStop = false;
            this.gbxDe.Text = "REMITENTES:";
            // 
            // btnActualizarRemitente
            // 
            this.btnActualizarRemitente.Enabled = false;
            this.btnActualizarRemitente.Location = new System.Drawing.Point(136, 21);
            this.btnActualizarRemitente.Name = "btnActualizarRemitente";
            this.btnActualizarRemitente.Size = new System.Drawing.Size(136, 23);
            this.btnActualizarRemitente.TabIndex = 2;
            this.btnActualizarRemitente.Text = "Actualizar Remitente";
            this.btnActualizarRemitente.UseVisualStyleBackColor = true;
            this.btnActualizarRemitente.Click += new System.EventHandler(this.btnActualizarRemitente_Click);
            // 
            // btnEliminarRemitente
            // 
            this.btnEliminarRemitente.Enabled = false;
            this.btnEliminarRemitente.Location = new System.Drawing.Point(278, 21);
            this.btnEliminarRemitente.Name = "btnEliminarRemitente";
            this.btnEliminarRemitente.Size = new System.Drawing.Size(140, 23);
            this.btnEliminarRemitente.TabIndex = 1;
            this.btnEliminarRemitente.Text = "Eliminar Remitente";
            this.btnEliminarRemitente.UseVisualStyleBackColor = true;
            this.btnEliminarRemitente.Click += new System.EventHandler(this.btnEliminarRemitente_Click);
            // 
            // btnInsActDe
            // 
            this.btnInsActDe.Enabled = false;
            this.btnInsActDe.Location = new System.Drawing.Point(6, 21);
            this.btnInsActDe.Name = "btnInsActDe";
            this.btnInsActDe.Size = new System.Drawing.Size(124, 23);
            this.btnInsActDe.TabIndex = 0;
            this.btnInsActDe.Text = "Insertar Remitente";
            this.btnInsActDe.UseVisualStyleBackColor = true;
            this.btnInsActDe.Click += new System.EventHandler(this.btnInsActDe_Click);
            // 
            // groupBox1
            // 
            this.groupBox1.Controls.Add(this.chbCopias);
            this.groupBox1.Controls.Add(this.btnEliminarCopia);
            this.groupBox1.Controls.Add(this.btnActualizarCopia);
            this.groupBox1.Controls.Add(this.btnInsertarCopia);
            this.groupBox1.Location = new System.Drawing.Point(454, 444);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(407, 47);
            this.groupBox1.TabIndex = 11;
            this.groupBox1.TabStop = false;
            this.groupBox1.Text = "COPIAS";
            // 
            // chbCopias
            // 
            this.chbCopias.AutoSize = true;
            this.chbCopias.Checked = true;
            this.chbCopias.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chbCopias.Location = new System.Drawing.Point(59, 0);
            this.chbCopias.Name = "chbCopias";
            this.chbCopias.Size = new System.Drawing.Size(15, 14);
            this.chbCopias.TabIndex = 3;
            this.chbCopias.UseVisualStyleBackColor = true;
            this.chbCopias.CheckStateChanged += new System.EventHandler(this.chbCopias_CheckStateChanged);
            // 
            // btnEliminarCopia
            // 
            this.btnEliminarCopia.Enabled = false;
            this.btnEliminarCopia.Location = new System.Drawing.Point(268, 17);
            this.btnEliminarCopia.Name = "btnEliminarCopia";
            this.btnEliminarCopia.Size = new System.Drawing.Size(133, 23);
            this.btnEliminarCopia.TabIndex = 2;
            this.btnEliminarCopia.Text = "Eliminar Copia";
            this.btnEliminarCopia.UseVisualStyleBackColor = true;
            this.btnEliminarCopia.Click += new System.EventHandler(this.btnEliminarCopia_Click);
            // 
            // btnActualizarCopia
            // 
            this.btnActualizarCopia.Enabled = false;
            this.btnActualizarCopia.Location = new System.Drawing.Point(137, 17);
            this.btnActualizarCopia.Name = "btnActualizarCopia";
            this.btnActualizarCopia.Size = new System.Drawing.Size(125, 23);
            this.btnActualizarCopia.TabIndex = 1;
            this.btnActualizarCopia.Text = "Actualizar Copia";
            this.btnActualizarCopia.UseVisualStyleBackColor = true;
            this.btnActualizarCopia.Click += new System.EventHandler(this.btnActualizarCopia_Click);
            // 
            // btnInsertarCopia
            // 
            this.btnInsertarCopia.Location = new System.Drawing.Point(8, 17);
            this.btnInsertarCopia.Name = "btnInsertarCopia";
            this.btnInsertarCopia.Size = new System.Drawing.Size(118, 23);
            this.btnInsertarCopia.TabIndex = 0;
            this.btnInsertarCopia.Text = "Insertar Copia";
            this.btnInsertarCopia.UseVisualStyleBackColor = true;
            this.btnInsertarCopia.Click += new System.EventHandler(this.btnInsertarCopia_Click);
            // 
            // groupBox2
            // 
            this.groupBox2.Controls.Add(this.chbReferencias);
            this.groupBox2.Controls.Add(this.chbAFisico);
            this.groupBox2.Controls.Add(this.chbAElectronico);
            this.groupBox2.Location = new System.Drawing.Point(220, 7);
            this.groupBox2.Name = "groupBox2";
            this.groupBox2.Size = new System.Drawing.Size(326, 47);
            this.groupBox2.TabIndex = 12;
            this.groupBox2.TabStop = false;
            this.groupBox2.Text = "DETALLES";
            // 
            // chbReferencias
            // 
            this.chbReferencias.AutoSize = true;
            this.chbReferencias.Checked = true;
            this.chbReferencias.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chbReferencias.Location = new System.Drawing.Point(239, 21);
            this.chbReferencias.Name = "chbReferencias";
            this.chbReferencias.Size = new System.Drawing.Size(78, 17);
            this.chbReferencias.TabIndex = 5;
            this.chbReferencias.Text = "Referencia";
            this.chbReferencias.UseVisualStyleBackColor = true;
            this.chbReferencias.CheckedChanged += new System.EventHandler(this.chbReferencias_CheckedChanged);
            // 
            // chbAFisico
            // 
            this.chbAFisico.AutoSize = true;
            this.chbAFisico.Checked = true;
            this.chbAFisico.CheckState = System.Windows.Forms.CheckState.Checked;
            this.chbAFisico.Location = new System.Drawing.Point(9, 21);
            this.chbAFisico.Name = "chbAFisico";
            this.chbAFisico.Size = new System.Drawing.Size(98, 17);
            this.chbAFisico.TabIndex = 2;
            this.chbAFisico.Text = "Anexos Físicos";
            this.chbAFisico.UseVisualStyleBackColor = true;
            this.chbAFisico.CheckedChanged += new System.EventHandler(this.chbAFisico_CheckedChanged);
            // 
            // chbAElectronico
            // 
            this.chbAElectronico.AutoSize = true;
            this.chbAElectronico.Location = new System.Drawing.Point(113, 21);
            this.chbAElectronico.Name = "chbAElectronico";
            this.chbAElectronico.Size = new System.Drawing.Size(122, 17);
            this.chbAElectronico.TabIndex = 1;
            this.chbAElectronico.Text = "Anexos Electrónicos";
            this.chbAElectronico.UseVisualStyleBackColor = true;
            this.chbAElectronico.CheckedChanged += new System.EventHandler(this.chbAElectronico_CheckedChanged);
            // 
            // gbxTipo
            // 
            this.gbxTipo.Controls.Add(this.rbtConfidencial);
            this.gbxTipo.Controls.Add(this.rbtNormal);
            this.gbxTipo.Location = new System.Drawing.Point(14, 7);
            this.gbxTipo.Name = "gbxTipo";
            this.gbxTipo.Size = new System.Drawing.Size(185, 47);
            this.gbxTipo.TabIndex = 13;
            this.gbxTipo.TabStop = false;
            this.gbxTipo.Text = "TIPO DE MEMORANDO:";
            // 
            // rbtConfidencial
            // 
            this.rbtConfidencial.AutoSize = true;
            this.rbtConfidencial.Location = new System.Drawing.Point(90, 19);
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
            this.rbtNormal.Location = new System.Drawing.Point(26, 19);
            this.rbtNormal.Name = "rbtNormal";
            this.rbtNormal.Size = new System.Drawing.Size(58, 17);
            this.rbtNormal.TabIndex = 0;
            this.rbtNormal.TabStop = true;
            this.rbtNormal.Text = "Normal";
            this.rbtNormal.UseVisualStyleBackColor = true;
            this.rbtNormal.CheckedChanged += new System.EventHandler(this.rbtNormal_CheckedChanged);
            // 
            // lblVersioAddInME
            // 
            this.lblVersioAddInME.AutoSize = true;
            this.lblVersioAddInME.Location = new System.Drawing.Point(603, 7);
            this.lblVersioAddInME.Name = "lblVersioAddInME";
            this.lblVersioAddInME.Size = new System.Drawing.Size(76, 13);
            this.lblVersioAddInME.TabIndex = 14;
            this.lblVersioAddInME.Text = "Versión AddIn:";
            // 
            // lblVersionPlantillaME
            // 
            this.lblVersionPlantillaME.AutoSize = true;
            this.lblVersionPlantillaME.Location = new System.Drawing.Point(603, 26);
            this.lblVersionPlantillaME.Name = "lblVersionPlantillaME";
            this.lblVersionPlantillaME.Size = new System.Drawing.Size(84, 13);
            this.lblVersionPlantillaME.TabIndex = 15;
            this.lblVersionPlantillaME.Text = "Versión Plantilla:";
            // 
            // SearchForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(983, 558);
            this.Controls.Add(this.lblVersionPlantillaME);
            this.Controls.Add(this.lblVersioAddInME);
            this.Controls.Add(this.gbxTipo);
            this.Controls.Add(this.groupBox2);
            this.Controls.Add(this.groupBox1);
            this.Controls.Add(this.gbxDe);
            this.Controls.Add(this.gbxPara);
            this.Controls.Add(this.lblDpto);
            this.Controls.Add(this.txtDpto);
            this.Controls.Add(this.lblCargo);
            this.Controls.Add(this.txtCargo);
            this.Controls.Add(this.lblNombre);
            this.Controls.Add(this.btnLimpiar);
            this.Controls.Add(this.txtNombre);
            this.Controls.Add(this.dgvUsuarios);
            this.Name = "SearchForm";
            this.Text = "Módulo de Memorando";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.SearchForm_FormClosing);
            this.Load += new System.EventHandler(this.SearchForm_Load);
            ((System.ComponentModel.ISupportInitialize)(this.dgvUsuarios)).EndInit();
            this.gbxPara.ResumeLayout(false);
            this.gbxDe.ResumeLayout(false);
            this.groupBox1.ResumeLayout(false);
            this.groupBox1.PerformLayout();
            this.groupBox2.ResumeLayout(false);
            this.groupBox2.PerformLayout();
            this.gbxTipo.ResumeLayout(false);
            this.gbxTipo.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button btInsertarPara;
        private System.Windows.Forms.DataGridView dgvUsuarios;
        private System.Windows.Forms.TextBox txtNombre;
        private System.Windows.Forms.Button btnLimpiar;
        private System.Windows.Forms.Label lblNombre;
        private System.Windows.Forms.TextBox txtCargo;
        private System.Windows.Forms.Label lblCargo;
        private System.Windows.Forms.TextBox txtDpto;
        private System.Windows.Forms.Label lblDpto;
        private System.Windows.Forms.GroupBox gbxPara;
        private System.Windows.Forms.Button btnActualizarPara;
        private System.Windows.Forms.Button btnEliminarPara;
        private System.Windows.Forms.GroupBox gbxDe;
        private System.Windows.Forms.Button btnInsActDe;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.CheckBox chbCopias;
        private System.Windows.Forms.Button btnEliminarCopia;
        private System.Windows.Forms.Button btnActualizarCopia;
        private System.Windows.Forms.Button btnInsertarCopia;
        private System.Windows.Forms.GroupBox groupBox2;
        private System.Windows.Forms.GroupBox gbxTipo;
        private System.Windows.Forms.RadioButton rbtConfidencial;
        private System.Windows.Forms.RadioButton rbtNormal;
        private System.Windows.Forms.CheckBox chbAElectronico;
        private System.Windows.Forms.CheckBox chbAFisico;
        private System.Windows.Forms.CheckBox chbReferencias;
        private System.Windows.Forms.Button btnEliminarRemitente;
        private System.Windows.Forms.Button btnActualizarRemitente;
        private System.Windows.Forms.Label lblVersioAddInME;
        private System.Windows.Forms.Label lblVersionPlantillaME;
    }
}