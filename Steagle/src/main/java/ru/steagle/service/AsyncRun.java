package ru.steagle.service;

public interface AsyncRun<T> {
	void onSuccess(T data);
	void onFailure(); 
}
