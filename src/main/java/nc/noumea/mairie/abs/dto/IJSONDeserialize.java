package nc.noumea.mairie.abs.dto;

public interface IJSONDeserialize<T> {
	public T deserializeFromJSON(String json);
}
