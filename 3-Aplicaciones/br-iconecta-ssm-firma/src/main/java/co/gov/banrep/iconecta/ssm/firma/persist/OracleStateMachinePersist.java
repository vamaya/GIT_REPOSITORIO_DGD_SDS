package co.gov.banrep.iconecta.ssm.firma.persist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.NoSuchElementException;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.kryo.StateMachineContextSerializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import co.gov.banrep.iconecta.ssm.firma.enums.Events;
import co.gov.banrep.iconecta.ssm.firma.enums.States;
import co.gov.banrep.iconecta.ssm.firma.params.GlobalConsts;

@SuppressWarnings("rawtypes")
public class OracleStateMachinePersist implements StateMachinePersist<States, Events, String>{
	
	@Autowired
	private MachineRepository repository;
	
	private static Kryo kryo;
	
	static {
		kryo = new Kryo();
		kryo.addDefaultSerializer(StateMachineContext.class, new StateMachineContextSerializer());	
		
		//((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy()).setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
		kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
	
	}

	@Override
	public void write(StateMachineContext<States, Events> context, String idMaquina) throws Exception {
		long id;
		
		String asunto = context.getExtendedState().get(GlobalConsts.ASUNTO, String.class);
		int idCircuito = context.getExtendedState().get(GlobalConsts.ID_CIRCUITO, Integer.class);
		boolean copiaCompulsada = context.getExtendedState().get(GlobalConsts.COPIA_COMPULSADA, Boolean.class);
		boolean errorMaquina = context.getExtendedState().get(GlobalConsts.ERROR_MAQUINA, Boolean.class);
		int reintento = context.getExtendedState().get(GlobalConsts.REINTENTO, Integer.class);
		String solicitante = context.getExtendedState().get(GlobalConsts.CONEXION_USUARIO_CS, String.class);
		String estadoActual = context.getState().name();
		
		try {
			id = repository.findByIdMaquina(idMaquina).stream().findFirst().get().getId();
			// repository.save(new Maquina(id, idMaquina, serialize(context),			
			repository.save(new Maquina(id, idMaquina, new Date(), solicitante, asunto, idCircuito, copiaCompulsada,
					errorMaquina, reintento, serialize(context), estadoActual));
		} catch (NoSuchElementException e) {
			//repository.save(new Maquina(idMaquina, serialize(context), context.getState().name(), new Date()));
			repository.save(new Maquina(idMaquina, new Date(), solicitante, asunto, idCircuito, copiaCompulsada,
					errorMaquina, reintento, serialize(context), estadoActual));			
		}
	}

	@Override
	public StateMachineContext<States, Events> read(String idMaquina) {
				
		Maquina maquina = repository.findByIdMaquina(idMaquina).stream().findFirst().get();
		byte[] byteContext = maquina.getContexto();
		return deserialize(byteContext);
	}
	
	private synchronized byte[] serialize(StateMachineContext<States, Events> context){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Output output = new Output(out);
		kryo.writeObject(output, context);
		output.flush();
		//output.close();		
		return out.toByteArray();
	}
	
	@SuppressWarnings("unchecked")
	private synchronized StateMachineContext<States, Events> deserialize(byte[] data){
		if (data == null || data.length == 0) {
			return null;
		}
		
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		Input input = new Input(in);
		return kryo.readObject(	input,StateMachineContext.class);
	}
	
	/*
	public void cambiarEstado(String idMaquina, States estado){
		Optional<Maquina> optMaquina = repository.findByIdMaquina(idMaquina).stream().findFirst();
		Maquina unaMaquina = null;
		
		if(optMaquina.isPresent()){
			unaMaquina = optMaquina.get();
			unaMaquina.setEstadoActual(estado.toString());
			repository.save(unaMaquina);
		}
	}
	*/

}
