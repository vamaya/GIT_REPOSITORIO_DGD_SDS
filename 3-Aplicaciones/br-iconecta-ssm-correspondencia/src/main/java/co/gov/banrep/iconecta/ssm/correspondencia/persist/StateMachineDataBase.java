package co.gov.banrep.iconecta.ssm.correspondencia.persist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.NoSuchElementException;

import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.kryo.StateMachineContextSerializer;
import org.springframework.util.StringUtils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import co.gov.banrep.iconecta.ssm.correspondencia.enums.Events;
import co.gov.banrep.iconecta.ssm.correspondencia.enums.States;
import co.gov.banrep.iconecta.ssm.correspondencia.utils.Consts;



@SuppressWarnings("rawtypes")
public class StateMachineDataBase implements StateMachinePersist<States, Events, String> {


	
	@Autowired
	private MachineRepository repository;
	
	@Autowired
	private PruebaEntregaRepository repositoryPrueba;
	
	private static Kryo kryo;
	
	static {
		kryo = new Kryo();
		kryo.addDefaultSerializer(StateMachineContext.class, new StateMachineContextSerializer());	
		
		((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy()).setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
	
	}
	
	@Override
	public void write(StateMachineContext<States, Events> context, String idMaquina) throws Exception {
		long id;
		
		
		String asunto = context.getExtendedState().get(Consts.ASUNTO, String.class);
		if(asunto==null || asunto.isEmpty() )
			asunto=context.getExtendedState().get(Consts.ASUNTO_RDI_RDE, String.class);
		
		boolean errorMaquina = context.getExtendedState().get(Consts.ERROR_MAQUINA, Boolean.class);
		int reintento = 0;
		String solicitante = context.getExtendedState().get(Consts.CONEXION_CS_SOLICITANTE, String.class);
		String numeroRadicado = context.getExtendedState().get(Consts.NUMERO_RADICADO_OBTENIDO, String.class);
		String nombreDocOriginal = context.getExtendedState().get(Consts.NOMBRE_DOCUMENTO_ORIGINAL, String.class);
		Long idDocumento = context.getExtendedState().get(Consts.ID_DOC_RADICACION, Long.class);
		String estadoActual = context.getState().name();
		
		if (StringUtils.hasText(asunto)) {
			if (asunto.length() > 250) {
				asunto = asunto.substring(0, 250);
			}
		}

		if (StringUtils.hasText(nombreDocOriginal)) {
			if (nombreDocOriginal.length() > 250) {
				nombreDocOriginal = nombreDocOriginal.substring(0, 250);
			}
		}
		
		try {
			id = repository.findByIdMaquina(idMaquina).stream().findFirst().get().getId();
			// repository.save(new MaquinaCorrespondencia(id, idMaquina, serialize(context),			
			repository.save(new MaquinaCorrespondencia(id, idMaquina, new Date(), solicitante, asunto,
					errorMaquina, reintento, serialize(context), estadoActual,idDocumento,numeroRadicado, nombreDocOriginal));
			if (estadoActual.equalsIgnoreCase("DISTRIBUIR_CARTA")|| estadoActual.equalsIgnoreCase("OBTENER_RESPUESTA_DISTRIBUIR_CDD_CARTA") ) {
				String tipologia = context.getExtendedState().get(Consts.TIPOLOGIA_MEMO_CARTA, String.class);
				String destino = context.getExtendedState().get(Consts.DESTINO_INDIVIDUAL_CARTA, String.class);
				if(tipologia.equalsIgnoreCase("CA"))
					repositoryPrueba.save(new PruebaEntrega(idMaquina, numeroRadicado, nombreDocOriginal, "", "", solicitante, destino, new Date(), null));
			}
		} catch (NoSuchElementException e) {
			//repository.save(new MaquinaCorrespondencia(idMaquina, serialize(context), context.getState().name(), new Date()));
			repository.save(new MaquinaCorrespondencia(idMaquina, new Date(), solicitante, asunto, 
					errorMaquina, reintento, serialize(context), estadoActual, idDocumento,numeroRadicado, nombreDocOriginal));
		}
		
	}

	@Override
	public StateMachineContext<States, Events> read(String idMaquina) throws Exception {
		MaquinaCorrespondencia maquinaCorrespondencia = repository.findByIdMaquina(idMaquina).stream().findFirst().get();
		byte[] byteContext = maquinaCorrespondencia.getContexto();
		return deserialize(byteContext);
	}
	
	private synchronized byte[] serialize(StateMachineContext<States, Events> context){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Output output = new Output(out);
		kryo.writeObject(output, context);
		//output.close();
		output.flush();
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


}
