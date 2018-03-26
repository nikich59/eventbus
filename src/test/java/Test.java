import ru.nikich59.eventbus.EventController;
import ru.nikich59.eventbus.EventHandler;
import ru.nikich59.eventbus.EventListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;


public class Test extends EventListener
{
	private ConcurrentLinkedQueue < Integer > ints = new ConcurrentLinkedQueue <>( );

	public Test( EventController eventController )
	{
		super( eventController );
	}

	public static void main( String[] args )
			throws Exception
	{
		EventController eventController = new EventController( );

		Test test = new Test( eventController );

//		eventController.subscribeEventListener( test );

		System.out.println( test.unsubscribe( ) );

//		eventController.distributeEvent( 3 );
		eventController.distributeEvent( new LinkedList <>( ) );
//		eventController.distributeEvent( new ArrayList <>( ) );
//		eventController.distributeEvent( new HashMap <>( ) );

		Thread.sleep( 50 );
		test.print( );
	}

	public void print( )
	{
		for ( int i : ints )
		{
			System.out.println( i );
		}
	}

	@EventHandler( overridable = true )
	public void eventHandler1( Object e )
	{
		ints.add( 1 );
//		System.out.println( "First  event handler" );
	}

	@EventHandler
	public void eventHandler2( List e )
	{
		ints.add( 2 );
//		System.out.println( "Second event handler" );
	}

	@EventHandler
	public void eventHandler3( ArrayList e )
	{
		ints.add( 3 );
//		System.out.println( "Third  event handler" );
	}
}
