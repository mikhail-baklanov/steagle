package ru.steagle.service;

public interface IRequestDataCommand {
	boolean canRun();
	boolean isTerminated();
	void run();
}
