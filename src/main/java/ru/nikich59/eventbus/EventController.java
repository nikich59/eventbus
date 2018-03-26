package ru.nikich59.eventbus;


import javafx.util.Pair;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventController
{
	private class EventDistributor implements Runnable
	{
		@Override
		public void run( )
		{
			isDistributionOn.set( true );

			while ( isDistributionOn.get( ) )
			{
				if ( events.isEmpty( ) )
				{
					isDistributionOn.set( false );

					break;
				}

				Object event = events.poll( );

				distributeEvent( event );
			}
		}

		private void distributeEvent( Object event )
		{
			try
			{
				for ( EventListener eventListener : eventListeners )
				{
					handleEvent( event, eventListener );
				}
			}
			catch ( Exception e )
			{
				exceptionHandler.handle( e );
			}
		}

		private void handleEvent( Object event, EventListener listener )
		{
			List < Pair < Method, Class > > eventHandlerCandidates = new ArrayList <>( );

			for ( Method method : listener.getClass( ).getDeclaredMethods( ) )
			{
				if ( method.isAnnotationPresent( EventHandler.class ) )
				{
					if ( ! method.getReturnType( ).equals( void.class ) ||
							method.getParameterTypes( ).length != 1 ||
							Modifier.isStatic( method.getModifiers( ) ) )
					{
						listener.handleException( new RuntimeException(
								"Event handlers are contracted to have the following prototype: " +
										"public void <eventHandler>(<EventClass> event)" ) );
					}
					Class eventClass = method.getParameterTypes( )[ 0 ];

					if ( eventClass.isAssignableFrom( event.getClass( ) ) )
					{
						eventHandlerCandidates.add( new Pair <>( method, eventClass ) );
					}
				}
			}

			eventHandlerCandidates.sort( ( e1, e2 ) ->
					{
						if ( e1.getValue( ).isAssignableFrom( e2.getValue( ) ) )
						{
							return 1;
						}
						else if ( e2.getValue( ).equals( e1.getValue( ) ) )
						{
							return 0;
						}

						return - 1;
					}
			);

			boolean isHandlerCalled = false;

			for ( int eventHandlerIndex = 0;
				  eventHandlerIndex < eventHandlerCandidates.size( );
				  eventHandlerIndex += 1 )
			{
				Pair < Method, Class > eventHandlerCandidate = eventHandlerCandidates.get( eventHandlerIndex );

				EventHandler eventHandlerDescription = eventHandlerCandidate.getKey( )
						.getAnnotation( EventHandler.class );

				if ( eventHandlerDescription.overridable( ) && isHandlerCalled )
				{
					continue;
				}

				isHandlerCalled = true;

				try
				{
					eventHandlerCandidate.getKey( ).invoke( listener, event );
				}
				catch ( Exception e )
				{
					listener.handleException( e );
				}
			}
		}
	}


	private EventListener.ExceptionHandler exceptionHandler = ( e ) ->
	{
		throw new RuntimeException( e );
	};

	private AtomicBoolean isDistributionOn = new AtomicBoolean( false );

	private ConcurrentLinkedQueue < EventListener > eventListeners = new ConcurrentLinkedQueue <>( );

	private ConcurrentLinkedQueue < Object > events = new ConcurrentLinkedQueue <>( );

	private Thread eventDistributionThread;

	public void distributeEvent( Object event )
	{
		events.add( event );

		runEventDistribution( );
	}

	public void subscribeEventListener( EventListener eventListener )
	{
		if ( ! eventListeners.contains( eventListener ) )
		{
			eventListeners.add( eventListener );
		}
	}

	public boolean unsubscribeEventListener( EventListener eventListener )
	{
		return eventListeners.remove( eventListener );
	}

	private void runEventDistribution( )
	{
		if ( ! isDistributionOn.get( ) )
		{
			isDistributionOn.set( true );

			eventDistributionThread = new Thread( new EventDistributor( ) );

			eventDistributionThread.start( );
		}
	}
}
