package ru.cyberspacelabs.nocounterfeit.contracts.api.base;

/**
 * Created by mike on 23.06.16.
 */
public interface AsynchronousService<C extends AsynchronousResultCallback> {
	void addCallback(C callback);

	void removeCallback(C callback);
}
