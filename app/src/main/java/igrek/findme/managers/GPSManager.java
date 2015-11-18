package igrek.findme.managers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
    boolean gps_enabled = false;
    boolean network_enabled = false;
    Location lastGPSLocation = null;
    Location lastNetworkLocation = null;
    long GPSTimeOffset = 0; //przesunięcie w czasie: GPSTime = SystemTime + offset
    long NetworkTimeOffset = 0; //przesunięcie w czasie: NetworkTime = SystemTime + offset
    int gps_satellites = 0;

    public GPSManager(Activity activity) throws Exception {
        this.activity = activity;
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Output.errorCritical("Błąd usługi lokalizacji");
        }
        String providers_str = "";
        for (String provider : locationManager.getAllProviders()) {
            providers_str += " " + provider + ",";
        }
        Output.info("Dostępne lokalizatory:" + providers_str);
        locationManager.addGpsStatusListener(this);
        locationManager.addNmeaListener(this);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Config.Location.min_updates_time, Config.Location.min_updates_distance, this);
            gps_enabled = true;
            Output.info("GPS jest dostępny.");
        } else {
            Output.info("GPS jest niedostępny!");
            //włączanie GPS - okno ustawień
            //activity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Config.Location.min_updates_time, Config.Location.min_updates_distance, this);
            network_enabled = true;
            Output.info("Lokalizacja przez Internet dostępna.");
        }
        if (!gps_enabled && !network_enabled) {
            Output.errorthrow("Brak jakiejkolwiek włączonej metody lokalizacji (GPS lub Internet)");
        }
        Output.info("Moduł lokalizacji pomyślnie uruchomiony.");
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
        if (event == GpsStatus.GPS_EVENT_STARTED) {
            Output.info("GPS: GPS_EVENT_STARTED");
        } else if (event == GpsStatus.GPS_EVENT_STOPPED) {
            lastGPSLocation = null;
            gps_satellites = 0;
            Output.info("GPS: GPS_EVENT_STOPPED");
        } else if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS || event == GpsStatus.GPS_EVENT_FIRST_FIX) {
            GpsStatus status = locationManager.getGpsStatus(null);
            if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
                lastGPSLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                GPSTimeOffset = lastGPSLocation.getTime() - System.currentTimeMillis();
                Output.info("GPS: Time to first fix: " + status.getTimeToFirstFix() + " ms");
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
            gps_satellites = satellites;
            Output.info("GPS: Liczba satelit: " + satellites);
        }
    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        Output.info("NmeaReceived");
        Output.log("timestamp: " + timestamp + ", NMEA: " + nmea);
    }

    @Override
    public void onLocationChanged(Location loc) {
        if (loc.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            lastGPSLocation = loc;
            GPSTimeOffset = lastGPSLocation.getTime() - System.currentTimeMillis();
            Output.info("Lokalizacja (GPS): " + loc.getLongitude() + ", " + loc.getLatitude() + " (" + loc.getExtras().getInt("satellites") + ")");
        } else if (loc.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            lastNetworkLocation = loc;
            NetworkTimeOffset = lastNetworkLocation.getTime() - System.currentTimeMillis();
            Output.info("Lokalizacja (Internet): " + loc.getLongitude() + ", " + loc.getLatitude());
        } else {
            Output.info("Nieznany provider: " + loc.getProvider());
        }
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
        Output.info("Location ProviderDisabled: " + provider);
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            lastGPSLocation = null;
        } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            lastNetworkLocation = null;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Output.info("Location ProviderEnabled: " + provider);
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
        return lastGPSLocation;
    }

    public Location getInternetLocation() {
        return lastNetworkLocation;
    }

    public Location getLocation() throws Exception {
        if (isGPSAvailable()) return getGPSLocation();
        if (isInternetAvailable()) return getInternetLocation();
        Output.errorthrow("Brak ostatniej lokalizacji");
        return null;
    }

    public boolean isGPSAvailable() {
        if (lastGPSLocation == null) return false;
        return System.currentTimeMillis() + GPSTimeOffset <= lastGPSLocation.getTime() + Config.Location.expired_time;
    }

    public boolean isInternetAvailable() {
        if (lastNetworkLocation == null) return false;
        return System.currentTimeMillis() + NetworkTimeOffset <= lastNetworkLocation.getTime() + Config.Location.expired_time;
    }

    public boolean isLocationAvailable() {
        return isGPSAvailable() || isInternetAvailable();
    }

    public boolean isGPSEnabled(){
        return gps_enabled;
    }

    public int getGPSSatellites(){
        return gps_satellites;
    }
}
