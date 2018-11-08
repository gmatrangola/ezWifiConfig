package org.freedesktop;

import java.util.List;
import java.util.Map;

import org.freedesktop.DBus.GLib.CSymbol;
import org.freedesktop.dbus.*;
import org.freedesktop.dbus.exceptions.DBusException;

public interface NetworkManager extends DBusInterface {
    public void Reload(UInt32 flags);

    public List<Path> GetDevices();

    @CSymbol("impl_manager_get_all_devices")
    public List<DBusInterface> GetAllDevices();

    public DBusInterface GetDeviceByIpIface(String iface);

    public DBusInterface ActivateConnection(DBusInterface connection, DBusInterface device, DBusInterface specific_object);

    public Pair<DBusInterface, DBusInterface> AddAndActivateConnection(Map<String, Map<String, Variant>> connection, DBusInterface device, DBusInterface specific_object);

    public void DeactivateConnection(DBusInterface active_connection);

    public void Sleep(boolean sleep);

    public void Enable(boolean enable);

    public Map<String, String> GetPermissions();

    public void SetLogging(String level, String domains);

    public Pair<String, String> GetLogging();

    public UInt32 CheckConnectivity();

    public UInt32 state();

    public interface Device extends DBusInterface {
        public interface Wired extends DBusInterface {
            public static class PropertiesChanged extends DBusSignal {
                public final Map<String, Variant> a;

                public PropertiesChanged(String path, Map<String, Variant> a) throws DBusException {
                    super(path, a);
                    this.a = a;
                }
            }
        }

        public interface Wireless extends DBusInterface {
            public void RequestScan(Map<String, Variant> options);

            public List<Path> GetAccessPoints();

            public static class AccessPointRemoved extends DBusSignal {
                public final DBusInterface a;

                public AccessPointRemoved(String path, DBusInterface a) throws DBusException {
                    super(path, a);
                    this.a = a;
                }
            }

            public static class AccessPointAdded extends DBusSignal {
                public final DBusInterface a;

                public AccessPointAdded(String path, DBusInterface a) throws DBusException {
                    super(path, a);
                    this.a = a;
                }
            }

            public static class PropertiesChanged extends DBusSignal {
                public final Map<String, Variant> a;

                public PropertiesChanged(String path, Map<String, Variant> a) throws DBusException {
                    super(path, a);
                    this.a = a;
                }
            }

        }
    }

    public interface AccessPoint extends DBusInterface {
        public static class PropertiesChanged extends DBusSignal {
            public final Map<String, Variant> properties;

            public PropertiesChanged(String path, Map<String, Variant> properties) throws DBusException {
                super(path, properties);
                this.properties = properties;
            }
        }
    }

    public interface Connection extends DBusInterface {
        public interface Active extends DBusInterface {
            public static class StateChanged extends DBusSignal {
                public StateChanged(String path, UInt32 state, UInt32 reason) throws DBusException {
                    super(path, state, reason);
                }
            }
            public static class PropertiesChanged extends DBusSignal {
                public final Map<String, Variant> properties;

                public PropertiesChanged(String path, Map<String, Variant> properties) throws DBusException {
                    super(path, properties);
                    this.properties = properties;
                }
            }
        }
    }


    public static class CheckPermissions extends DBusSignal {
        public CheckPermissions(String path) throws DBusException {
            super(path);
        }
    }

    public static class StateChanged extends DBusSignal {
        public final UInt32 state;

        public StateChanged(String path, UInt32 state) throws DBusException {
            super(path, state);
            this.state = state;
        }
    }

    public static class PropertiesChanged extends DBusSignal {
        public final Map<String, Variant> properties;

        public PropertiesChanged(String path, Map<String, Variant> properties) throws DBusException {
            super(path, properties);
            this.properties = properties;
        }
    }

    public static class DeviceAdded extends DBusSignal {
        public final DBusInterface device_path;

        public DeviceAdded(String path, DBusInterface device_path) throws DBusException {
            super(path, device_path);
            this.device_path = device_path;
        }
    }

    public static class DeviceRemoved extends DBusSignal {
        public final DBusInterface device_path;

        public DeviceRemoved(String path, DBusInterface device_path) throws DBusException {
            super(path, device_path);
            this.device_path = device_path;
        }
    }
}
