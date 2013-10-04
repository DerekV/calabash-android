package sh.calaba.instrumentationbackend;

import sh.calaba.instrumentationbackend.actions.Actions;
import sh.calaba.instrumentationbackend.actions.HttpServer;
import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.jayway.android.robotium.solo.PublicViewFetcher;
import com.jayway.android.robotium.solo.SoloEnhanced;

import java.io.File;
import java.lang.reflect.Method;

public class InstrumentationBackend extends ActivityInstrumentationTestCase2<Activity> {
    public static String testPackage;
    public static Class<? extends Activity> mainActivity;
    public static Bundle extras;
    
    private static final String TAG = "InstrumentationBackend";
    
    public static Instrumentation instrumentation;
    public static SoloEnhanced solo;
    public static PublicViewFetcher viewFetcher;
    public static Actions actions;
    public static File coverageDumpFile;

    @SuppressWarnings({ "deprecation", "unchecked" })
    public InstrumentationBackend() {
        super(testPackage, (Class<Activity>) mainActivity);
    }

    @Override
    protected void setUp() throws Exception {
        log("entering setUp");
        super.setUp();
        Intent i = new Intent();
        i.setClassName(testPackage, mainActivity.getName());
        i.putExtras(extras);
        setActivityIntent(i);
        solo = new SoloEnhanced(getInstrumentation());
        viewFetcher = new PublicViewFetcher(getInstrumentation(), this.getActivity());
        actions = new Actions(getInstrumentation(), this);
        instrumentation = getInstrumentation();
    }

    /**
     * Here to have JUnit3 start the instrumentationBackend
     */
    public void testHook() throws Exception {
        log("entering testHook");
        HttpServer httpServer = HttpServer.getInstance();
        httpServer.setReady();
        httpServer.waitUntilShutdown();
        solo.finishOpenedActivities();
        doEmma();
       // System.exit(0);
    }

    @Override
    public void tearDown() throws Exception {
        log("entering tearDown");
        HttpServer httpServer = HttpServer.getInstance();
        httpServer.stop();

        System.out.println("Finishing");
        try {
            solo.finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        removeTestLocationProviders(this.getActivity());

        this.getActivity().finish();
        super.tearDown();

    }

    public static void log(String message) {
        Log.i(TAG, message);
    }

    public static void logError(String message) {
        Log.e(TAG, message);
    }

    private void doEmma() {
        log("entering doEmma");
        log("emma coverage file is " + coverageDumpFile);
        try {
            Class emmaRTClass = Class.forName("com.vladium.emma.rt.RT");
            Method dumpCoverageMethod = emmaRTClass.getMethod("dumpCoverageData",
                    coverageDumpFile.getClass(), boolean.class, boolean.class);

            dumpCoverageMethod.invoke(null, coverageDumpFile, true, false);
            log("emma coverage Appears we wrote the coverage file to " + coverageDumpFile.toString());
        } catch (Exception e ) {
            logError("emma coverage exception" + e.getMessage());
            e.printStackTrace();
        }
        coverageDumpFile.setReadable(true,false);
    }

    private void removeTestLocationProviders(Activity activity) {
        int hasPermission = solo.getCurrentActivity().checkCallingOrSelfPermission(Manifest.permission.ACCESS_MOCK_LOCATION);

        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationService = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            for (final String provider : locationService.getAllProviders()) {
                locationService.removeTestProvider(provider);
            }
        }
    }
}
