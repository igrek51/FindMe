package igrek.findme.managers;

import android.app.Activity;
import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import igrek.findme.settings.Config;
import igrek.findme.system.Output;

public class GPSManager implements LocationListener, GpsStatus.Listener, GpsStatus.NmeaListener {
    Activity activity = null;
    LocationManager locationManager = null;
    boolean available = false; //TODO: kiedy zmieniać avaible? rozłączanie i łapanie sygnału

    public GPSManager(Activity activity) {
        this.activity = activity;
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Output.errorCritical("Błąd usługi lokalizacji");
            return;
        }
        Output.info("Available providers:");
        for (String provider : locationManager.getAllProviders()) {
            Output.info(provider);
        }
        try {
            locationManager.addGpsStatusListener(this);
            locationManager.addNmeaListener(this);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Config.geti().location.min_updates_time, Config.geti().location.min_updates_distance, this);
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Config.geti().location.min_updates_time, Config.geti().location.min_updates_distance, this);
            }
        } catch (RuntimeException ex) {
            Output.error(ex);
        }
        Output.info("Moduł lokalizacji uruchomiony.");
    }

    public String gpsStatusToString(int gpsstatus) {
        if (gpsstatus == GpsStatus.GPS_EVENT_FIRST_FIX) {
            return "GPS_EVENT_FIRST_FIX";
        } else if (gpsstatus == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            return "GPS_EVENT_SATELLITE_STATUS";
        } else if (gpsstatus == GpsStatus.GPS_EVENT_STARTED) {
            return "GPS_EVENT_STARTED";
        } else if (gpsstatus == GpsStatus.GPS_EVENT_STOPPED) {
            return "GPS_EVENT_STOPPED";
        }
        return "";
    }

    @Override
    public void onGpsStatusChanged(int event) {
        Output.info("GpsStatusChanged: " + gpsStatusToString(event));
        if(event==GpsStatus.GPS_EVENT_FIRST_FIX){
            available = true;
        }
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS || event == GpsStatus.GPS_EVENT_FIRST_FIX) {
            GpsStatus status = locationManager.getGpsStatus(null);
            if (status.getTimeToFirstFix() != 0) {
                Output.info("Time to first fix: " + status.getTimeToFirstFix());
            }
            Iterable<GpsSatellite> sats = status.getSatellites();
            int satellites = 0;
            Output.log("Satelity:");
            for (GpsSatellite sat : sats) {
                satellites++;
                String details = "";
                details += "Satellite " + satellites + " - usedInFix: " + sat.usedInFix() + ", azimuth: " + sat.getAzimuth() + ", elevation: " + sat.getElevation() + ", pseudoRandomNumber: " + sat.getPrn() + ", signalToNoiseRatio: " + sat.getSnr();
                details += ", Almanac: " + sat.hasAlmanac() + ", Ephemeris: " + sat.hasEphemeris();
                Output.log(details);
            }
            Output.info("Liczba satelit: " + satellites);
        }
    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        Output.info("NmeaReceived");
        Output.log("timestamp: " + timestamp + ", NMEA: " + nmea);
    }

    @Override
    public void onLocationChanged(Location loc) {
        Output.info("LocChanged: " + loc.getLongitude() + ", " + loc.getLatitude() + " (" + loc.getExtras().getInt("satellites") + ", " + loc.getProvider() + ")");
        String details = "";
        details += "Provider: " + loc.getProvider();
        details += ", accuracy: " + loc.getAccuracy();
        details += ", Time: " + loc.getTime();
        details += ", Longitude: " + loc.getLongitude();
        details += ", Latitude: " + loc.getLatitude();
        details += ", Altitude: " + loc.getAltitude();
        details += ", satellites used to derive the fix: " + loc.getExtras().getInt("satellites");
        Output.log(details);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Output.info("ProviderDisabled: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Output.info("ProviderEnabled: " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Output.info("StatusChanged: " + provider + ", status: " + status + " (" + gpsStatusToString(status) + " ?)");
        for (String key : extras.keySet()) {
            Object value = extras.get(key);
            Output.info("extras key: " + key + " = " + value.toString() + ", type: " + value.getClass().getName());
        }
    }

    public Location getGPSLocation() {
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    public boolean isGPSAvailable() {
        return available;
    }
}
