﻿//------------------------------------------------------------------------------
// <auto-generated>
//     Este código fue generado por una herramienta.
//     Versión de runtime:4.0.30319.42000
//
//     Los cambios en este archivo podrían causar un comportamiento incorrecto y se perderán si
//     se vuelve a generar el código.
// </auto-generated>
//------------------------------------------------------------------------------

namespace WordAddIn2.ServiceReference1 {
    
    
    [System.CodeDom.Compiler.GeneratedCodeAttribute("System.ServiceModel", "4.0.0.0")]
    [System.ServiceModel.ServiceContractAttribute(Namespace="http://CorrTest/", ConfigurationName="ServiceReference1.WSTest")]
    public interface WSTest {
        
        // CODEGEN: Se está generando un contrato de mensaje, ya que el nombre de elemento name del espacio de nombres  no está marcado para aceptar valores nil.
        [System.ServiceModel.OperationContractAttribute(Action="http://CorrTest/WSTest/helloRequest", ReplyAction="http://CorrTest/WSTest/helloResponse")]
        WordAddIn2.ServiceReference1.helloResponse hello(WordAddIn2.ServiceReference1.helloRequest request);
        
        [System.ServiceModel.OperationContractAttribute(Action="http://CorrTest/WSTest/helloRequest", ReplyAction="http://CorrTest/WSTest/helloResponse")]
        System.Threading.Tasks.Task<WordAddIn2.ServiceReference1.helloResponse> helloAsync(WordAddIn2.ServiceReference1.helloRequest request);
    }
    
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.CodeDom.Compiler.GeneratedCodeAttribute("System.ServiceModel", "4.0.0.0")]
    [System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Advanced)]
    [System.ServiceModel.MessageContractAttribute(IsWrapped=false)]
    public partial class helloRequest {
        
        [System.ServiceModel.MessageBodyMemberAttribute(Name="hello", Namespace="http://CorrTest/", Order=0)]
        public WordAddIn2.ServiceReference1.helloRequestBody Body;
        
        public helloRequest() {
        }
        
        public helloRequest(WordAddIn2.ServiceReference1.helloRequestBody Body) {
            this.Body = Body;
        }
    }
    
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.CodeDom.Compiler.GeneratedCodeAttribute("System.ServiceModel", "4.0.0.0")]
    [System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Advanced)]
    [System.Runtime.Serialization.DataContractAttribute(Namespace="")]
    public partial class helloRequestBody {
        
        [System.Runtime.Serialization.DataMemberAttribute(EmitDefaultValue=false, Order=0)]
        public string name;
        
        public helloRequestBody() {
        }
        
        public helloRequestBody(string name) {
            this.name = name;
        }
    }
    
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.CodeDom.Compiler.GeneratedCodeAttribute("System.ServiceModel", "4.0.0.0")]
    [System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Advanced)]
    [System.ServiceModel.MessageContractAttribute(IsWrapped=false)]
    public partial class helloResponse {
        
        [System.ServiceModel.MessageBodyMemberAttribute(Name="helloResponse", Namespace="http://CorrTest/", Order=0)]
        public WordAddIn2.ServiceReference1.helloResponseBody Body;
        
        public helloResponse() {
        }
        
        public helloResponse(WordAddIn2.ServiceReference1.helloResponseBody Body) {
            this.Body = Body;
        }
    }
    
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.CodeDom.Compiler.GeneratedCodeAttribute("System.ServiceModel", "4.0.0.0")]
    [System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Advanced)]
    [System.Runtime.Serialization.DataContractAttribute(Namespace="")]
    public partial class helloResponseBody {
        
        [System.Runtime.Serialization.DataMemberAttribute(EmitDefaultValue=false, Order=0)]
        public string @return;
        
        public helloResponseBody() {
        }
        
        public helloResponseBody(string @return) {
            this.@return = @return;
        }
    }
    
    [System.CodeDom.Compiler.GeneratedCodeAttribute("System.ServiceModel", "4.0.0.0")]
    public interface WSTestChannel : WordAddIn2.ServiceReference1.WSTest, System.ServiceModel.IClientChannel {
    }
    
    [System.Diagnostics.DebuggerStepThroughAttribute()]
    [System.CodeDom.Compiler.GeneratedCodeAttribute("System.ServiceModel", "4.0.0.0")]
    public partial class WSTestClient : System.ServiceModel.ClientBase<WordAddIn2.ServiceReference1.WSTest>, WordAddIn2.ServiceReference1.WSTest {
        
        public WSTestClient() {
        }
        
        public WSTestClient(string endpointConfigurationName) : 
                base(endpointConfigurationName) {
        }
        
        public WSTestClient(string endpointConfigurationName, string remoteAddress) : 
                base(endpointConfigurationName, remoteAddress) {
        }
        
        public WSTestClient(string endpointConfigurationName, System.ServiceModel.EndpointAddress remoteAddress) : 
                base(endpointConfigurationName, remoteAddress) {
        }
        
        public WSTestClient(System.ServiceModel.Channels.Binding binding, System.ServiceModel.EndpointAddress remoteAddress) : 
                base(binding, remoteAddress) {
        }
        
        [System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Advanced)]
        WordAddIn2.ServiceReference1.helloResponse WordAddIn2.ServiceReference1.WSTest.hello(WordAddIn2.ServiceReference1.helloRequest request) {
            return base.Channel.hello(request);
        }
        
        public string hello(string name) {
            WordAddIn2.ServiceReference1.helloRequest inValue = new WordAddIn2.ServiceReference1.helloRequest();
            inValue.Body = new WordAddIn2.ServiceReference1.helloRequestBody();
            inValue.Body.name = name;
            WordAddIn2.ServiceReference1.helloResponse retVal = ((WordAddIn2.ServiceReference1.WSTest)(this)).hello(inValue);
            return retVal.Body.@return;
        }
        
        [System.ComponentModel.EditorBrowsableAttribute(System.ComponentModel.EditorBrowsableState.Advanced)]
        System.Threading.Tasks.Task<WordAddIn2.ServiceReference1.helloResponse> WordAddIn2.ServiceReference1.WSTest.helloAsync(WordAddIn2.ServiceReference1.helloRequest request) {
            return base.Channel.helloAsync(request);
        }
        
        public System.Threading.Tasks.Task<WordAddIn2.ServiceReference1.helloResponse> helloAsync(string name) {
            WordAddIn2.ServiceReference1.helloRequest inValue = new WordAddIn2.ServiceReference1.helloRequest();
            inValue.Body = new WordAddIn2.ServiceReference1.helloRequestBody();
            inValue.Body.name = name;
            return ((WordAddIn2.ServiceReference1.WSTest)(this)).helloAsync(inValue);
        }
    }
}
