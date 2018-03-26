package ru.nikich59.eventbus;


public class EventListener
{
	private EventController eventController;

	public interface ExceptionHandler
	{
		void handle( Exception e );
	}

	private ExceptionHandler exceptionHandler = ( e ) ->
	{
		throw new RuntimeException( e );
	};

	public void setExceptionHandler( ExceptionHandler exceptionHandler )
	{
		this.exceptionHandler = exceptionHandler;
	}

	void handleException( Exception e )
	{
		exceptionHandler.handle( e );
	}

	public EventListener( EventController eventController )
	{
		this.eventController = eventController;

		eventController.subscribeEventListener( this );
	}

	public final boolean unsubscribe( )
	{
		return eventController.unsubscribeEventListener( this );
	}

	public final void emitEvent( Object event )
	{
		eventController.distributeEvent( event );
	}
}
