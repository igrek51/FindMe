package igrek.findme.system;

import android.app.Activity;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import igrek.findme.logic.Types;

public class LocationMaster implements LocationListener, GpsStatus.Listener, GpsStatus.NmeaListener {
    Activity activity = null;
    LocationManager locationManager = null;

    public LocationMaster(Activity activity) {
        this.activity = activity;
        locationManager = (LocationManager) activity.getSystemService(activity.LOCATION_SERVICE);
        if (locationManager == null) {
            Output.errorCritical("Błąd usługi lokacji");
            return;
        }
        Output.log("All providers:");
        for (String provider : locationManager.getAllProviders()) {
            Output.log(provider);
        }
        try {
            locationManager.addGpsStatusListener(this);
            locationManager.addNmeaListener(this);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        } catch (SecurityException | IllegalArgumentException ex) {
            Output.error(ex);
        } catch (RuntimeException ex) {
            Output.error(ex);
        }
        Output.log("Moduł lokacji uruchomiony.");
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
        Output.log("GpsStatusChanged: " + gpsStatusToString(event));
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS || event == GpsStatus.GPS_EVENT_FIRST_FIX) {
            GpsStatus status = locationManager.getGpsStatus(null);
            Output.log("Time to first fix: " + status.getTimeToFirstFix());
            Output.log("Max Satellites: " + status.getMaxSatellites());
            Iterable<GpsSatellite> sats = status.getSatellites();
            int satellites = 0;
            Output.log("Satelity:");
            for (GpsSatellite sat : sats) {
                satellites++;
                String details = "";
                details += "Satellite "+satellites+" - usedInFix: "+sat.usedInFix()+", azimuth: "+sat.getAzimuth()+", elevation: "+sat.getElevation()+", pseudoRandomNumber: "+sat.getPrn()+", signalToNoiseRatio: "+sat.getSnr();
                details += ", Almanac: "+sat.hasAlmanac()+", Ephemeris: "+sat.hasEphemeris();
                Output.log(details);
            }
        }
    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        Output.log("onNmeaReceived - timestamp: "+timestamp+", NMEA: "+nmea);
    }

    @Override
    public void onLocationChanged(Location loc) {
        String details = "onLocationChanged - ";
        details += "provider: "+loc.getProvider();
        details += ", accuracy: "+loc.getAccuracy();
        details += ", Time: "+loc.getTime();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            details += ", ElapsedRealtimeNanos: "+loc.getElapsedRealtimeNanos();
        }
        details += ", Longitude: "+loc.getLongitude();
        details += ", Latitude: "+loc.getLatitude();
        details += ", Altitude: "+loc.getAltitude();
        details += ", satellites used to derive the fix: "+loc.getExtras().getInt("satellites");
        Output.log(details);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Output.log("onProviderDisabled: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Output.log("onProviderEnabled: " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Output.log("onStatusChanged: " + provider + ", status: " + status);
        for (String key : extras.keySet()) {
            Object value = extras.get(key);
            Output.log("extras key: " + key + " = " + value.toString() + ", type: " + value.getClass().getName());
        }
    }

    public double getCoordinate(String provider, Types.Coordinate coordinate) {
        if (locationManager == null) {
            Output.error("location Manager = null");
            return 0;
        }
        Location location = null;
        try {
            location = locationManager.getLastKnownLocation(provider);
        } catch (SecurityException | IllegalArgumentException ex) {
            Output.error(ex);
        }
        if (location == null) {
            Output.error("last location (" + provider + ") = null");
            return 0;
        }
        if (coordinate == Types.Coordinate.ALTITUDE) {
            if (!location.hasAltitude()) {
                Output.error("location: brak altitude");
                return 0;
            }
            return location.getAltitude();
        } else if (coordinate == Types.Coordinate.LATITUDE) {
            return location.getLatitude();
        } else if (coordinate == Types.Coordinate.LONGITUDE) {
            return location.getLongitude();
        } else if (coordinate == Types.Coordinate.ACCURACY) {
            if (!location.hasAccuracy()) {
                Output.error("location: brak accuracy");
                return 0;
            }
            return (double) location.getAccuracy();
        }
        return 0;
    }

}
