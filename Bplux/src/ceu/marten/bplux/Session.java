package ceu.marten.bplux;

import android.text.format.Time;

public class Session {
	private String name;
	private float[] data;
	private String fechayhora;
	private Time duracion;

	
	
	public Session(String name, float[] data, String fechayhora, Time duracion) {
		super();
		this.name = name;
		this.data = data;
		this.fechayhora = fechayhora;
		this.duracion = duracion;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float[] getData() {
		return data;
	}

	public void setData(float[] data) {
		this.data = data;
	}

	public String getFechayhora() {
		return fechayhora;
	}

	public void setFechayhora(String fechayhora) {
		this.fechayhora = fechayhora;
	}

	public Time getDuracion() {
		return duracion;
	}

	public void setDuracion(Time duracion) {
		this.duracion = duracion;
	}

}
