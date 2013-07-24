package tests;

import java.beans.PropertyChangeEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import android.test.ActivityInstrumentationTestCase2;

import com.kovaciny.linemonitorbot.MainActivity;
import com.kovaciny.primexmodel.PrimexModel;
import com.kovaciny.primexmodel.Product;
import com.kovaciny.primexmodel.Sheet;

public class PrimexModelTest extends ActivityInstrumentationTestCase2<MainActivity> {

	MainActivity mActivity;
	PrimexModel mModel;
	
	public PrimexModelTest() {
		super(MainActivity.class);
	}
	
	@Before
	public void setUp() throws Exception {
		setActivityInitialTouchMode(false);
		
		mActivity = (MainActivity)getActivity();
	    
		mModel = new PrimexModel(mActivity);
		mModel.addPropertyChangeListener(mActivity);
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testLineRoundTrip() {
		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				// UI affecting code here
				// no asserts allowed in here! junit.framework.AssertionFailedError.
				mModel.setSelectedLine(12);
				Product p = new Sheet(.010, 40, 28);
				p.setUnitWeight(1.5);
				mModel.changeProduct(p);
//				mModel.addPropertyChangeListener()
				
				mModel.setSelectedLine(7);
				Product q = new Sheet(.010, 56, 80);
				q.setUnitWeight(2.5);
				mModel.changeProduct(q);
				mModel.setSelectedLine(12);
				//no sendKeys() or invoking context menu here, "this method cannot be called from the main application thread"
			}
		});
		getInstrumentation().waitForIdleSync();
		//asserts and this.sendKeys() OK here

		assertEquals(mModel.getSelectedWorkOrder().getProduct().getUnitWeight(), 1.5);

	}
	/*
	@Test
	public void testPrimexModel() {
		fail("Not yet implemented"); // TODO
	}
	
	@Test
	public void testSetSelectedLineInteger() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetSelectedLineProductionLine() {
		
	}

	@Test
	public void testSetSelectedWorkOrder() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testAddSkid() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testAddProduct() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testAddWorkOrder() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testAddWorkOrderWorkOrder() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetCurrentSpeed() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testChangeNovatecSetpoint() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testChangeProduct() {
		Product p = new Sheet(.010, 40, 28);
		p.setUnitWeight(1.5);
		mModel.changeProduct(p);
		
		
	}

	@Test
	public void testChangeSelectedSkid() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSaveProduct() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSaveSelectedLine() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSaveSkid() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSaveState() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testLoadState() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetProductsPerMinute() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetProductsPerMinute() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCalculateRates() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCalculateTimes() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetDatabaseVersion() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testChangeNumberOfSkids() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testCloseDb() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testHasSelectedLine() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testHasSelectedWorkOrder() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testHasSelectedProduct() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetSelectedLine() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetSelectedWorkOrder() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetLineNumbers() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetWoNumbers() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetAllWoNumbersForLine() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetHighestWoNumber() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testClearWoNumbers() {
		fail("Not yet implemented"); // TODO
	}
	*/
}