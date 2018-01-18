package ru.cyberspacelabs.nocounterfeit.async;

import java.util.UUID;

/**
 * Created by mike on 12.04.16.
 */
public abstract class Result<E> {
	private E value;
	private boolean successful;
	private Throwable cause;
	private UUID requestID;
	private UUID clientID;
	private String dispatchID;
	private String correlationID;

	public Result(UUID clientID, String dispatchID) {
		this.clientID = clientID;
		this.requestID = UUID.randomUUID();
		this.dispatchID = dispatchID;
	}

	public E getValue() {
		return value;
	}

	public void setValue(E value) {
		this.value = value;
		this.cause = null;
		setSuccessful(true);
	}

	public boolean isSuccessful() {
		return successful;
	}

	private void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public Throwable getCause() {
		return cause;
	}

	public void setCause(Throwable cause) {
		this.cause = cause;
		this.value = null;
		setSuccessful(false);
	}

	public UUID getRequestID() {
		return requestID;
	}

	public UUID getClientID() {
		return clientID;
	}

	public abstract Class<E> getEntityClass();

	public String getDispatchID() {
		return dispatchID;
	}

	public String getCorrelationID() {
		return correlationID;
	}

	public void setCorrelationID(String correlationID) {
		this.correlationID = correlationID;
	}
}
