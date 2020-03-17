package co.gov.banrep.iconecta.ssm.firma.persist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;

public class ContextoPersist {

	@Autowired
	private ContextoRepository repository;

	private static Kryo kryo;

	static {
		kryo = new Kryo();
		kryo.register(HashMap.class, new MapSerializer());
		//((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy())
		//		.setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
		kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));

	}

	public synchronized void write(String idMaquina, Map<Object, Object> variables) throws DataAccessException {
		try {
			long id = repository.findByIdMaquina(idMaquina).stream().findFirst().get().getId();
			repository.save(new Contexto(id, idMaquina, new Date(), serialize(variables)));
		} catch (NoSuchElementException e) {
			repository.save(new Contexto(idMaquina, new Date(), serialize(variables)));
		}
	}
	
	public synchronized Map<Object, Object> read(String idMaquina) {
		Contexto contexto = repository.findByIdMaquina(idMaquina).stream().findFirst().get();
		Map<Object, Object> variables = deserialize(contexto.getVariables());
		return variables;
	}
	
	public synchronized void delete(String idMaquina){
		long id = repository.findByIdMaquina(idMaquina).stream().findFirst().get().getId();
		repository.delete(id);
	}

	private synchronized byte[] serialize(Map<Object, Object> variables) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Output output = new Output(out);
		kryo.writeClassAndObject(output, variables);
		//kryo.writeObject(output, variables);
		output.flush();
		//output.close();
		return out.toByteArray();
	}

	@SuppressWarnings("unchecked")
	private synchronized Map<Object, Object> deserialize(byte[] data) {
		if (data == null || data.length == 0) {
			return null;
		}

		ByteArrayInputStream in = new ByteArrayInputStream(data);
		Input input = new Input(in);

		Map<Object, Object> unMap = (Map<Object, Object>) kryo.readClassAndObject(input);
		//Map<Object, Object> unMap = (Map<Object, Object>) kryo.readObject(input, HashMap.class);

		if (unMap == null) {
			throw new NullPointerException("unMap es nulo");
		}

		return unMap;
	}
}
