import requests
import random
import time
from datetime import datetime, timezone

BASE_URL = "http://localhost:8081/OntologyWebApp/api"


# ✅ STRICT xsd:dateTimeStamp format
def now_timestamp():
    return datetime.now(timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z")


def create_instance(class_name, name, data=None, obj=None):
    payload = {
        "individualName": name,
        "dataProperties": [],
        "objectProperties": []
    }

    if data:
        for k, v in data.items():
            payload["dataProperties"].append({
                "property": k,
                "value": str(v)
            })

    if obj:
        for k, v in obj.items():
            payload["objectProperties"].append({
                "property": k,
                "value": v
            })

    url = f"{BASE_URL}/{class_name}"

    try:
        r = requests.post(url, json=payload, timeout=30)
        if r.status_code in [200, 201]:
            print("Created:", name)
        else:
            print("Error:", name, r.text)
    except Exception as e:
        print("Request failed:", name, str(e))


# -------------------------------------------------
# 1 FACTORY
# -------------------------------------------------

create_instance(
    "Factory",
    "MEDWOOD_Factory",
    {
        "factoryID": "F001",
        "factoryName": "MEDWOOD Furniture Factory",
        "factoryOperator": "MEDWOOD Ltd"
    }
)


# -------------------------------------------------
# 2 CORE ASSETS
# -------------------------------------------------

create_instance(
    "Machine",
    "CNC_CuttingMachine_01",
    {
        "machineID": "M001",
        "machineName": "CNC Cutting Machine 01"
    },
    {
        "machineLocatedInFactory": "MEDWOOD_Factory"
    }
)

create_instance(
    "ProductionProcess",
    "FurnitureAssemblyProcess",
    {
        "processID": "P001",
        "processName": "Furniture Assembly Process"
    },
    {
        "executedInFactory": "MEDWOOD_Factory"
    }
)

create_instance("Shipment", "Shipment_RawMaterial_001")
create_instance("Shipment", "Shipment_Furniture_Truck01")


# -------------------------------------------------
# 3 PARAMETERS (STRICT — NO DOMAIN VIOLATIONS)
# -------------------------------------------------

create_instance(
    "PhysicalParameter",
    "MachineVibrationParameter",
    {
        "parameterID": "PP001",
        "parameterName": "Machine Vibration"
    }
)

create_instance(
    "LogisticsParameter",
    "TransitTimeParameter",
    {
        "parameterID": "LP001",
        "parameterName": "Transit Time"
    }
)

create_instance(
    "LogisticsParameter",
    "RouteDeviationParameter",
    {
        "parameterID": "LP002",
        "parameterName": "Route Deviation"
    }
)

create_instance(
    "ProductionMetric",
    "CycleTimeMetric",
    {
        "parameterID": "PM001",
        "parameterName": "Cycle Time",
        "targetValue": "20.0"
    },
    {
        "metricOfProcess": "FurnitureAssemblyProcess"
    }
)


# -------------------------------------------------
# 4 SENSORS (ENUM FIXED)
# -------------------------------------------------

create_instance(
    "ConditionMonitoringSensor",
    "VibrationSensor_CNC01",
    {
        "sensorID": "S001",
        "sensorName": "Vibration Sensor",
        "sensorType": "VibrationSensor",  # ✅ exact enum
        "status": "Active",
        "dataSourceType": "IoT",
        "conformsToStandard": "SSN/SOSA"
    },
    {
        "isDeployedOnMachine": "CNC_CuttingMachine_01",
        "monitorsParameter": "MachineVibrationParameter"
    }
)

create_instance(
    "ProductionMonitoringSensor",
    "CycleTimeSensor_Assembly",
    {
        "sensorID": "S002",
        "sensorName": "Cycle Time Sensor",
        "sensorType": "ProductionPerformanceSensor",  # ✅ exact enum
        "status": "Active",
        "dataSourceType": "IoT",
        "conformsToStandard": "SSN/SOSA"
    },
    {
        "monitorsParameter": "CycleTimeMetric",
        "monitorsProcess": "FurnitureAssemblyProcess"
    }
)

create_instance(
    "LogisticsSensor",
    "LogisticsSensor_TruckGPS_01",
    {
        "sensorID": "S003",
        "sensorName": "Truck GPS Sensor 01",
        "sensorType": "GPSSensor",  # ✅ FIXED (case-sensitive)
        "status": "Active",
        "dataSourceType": "IoT",
        "conformsToStandard": "SSN/SOSA"
    },
    {
        "isDeployedOnShipment": "Shipment_RawMaterial_001",
        "monitorsParameter": "TransitTimeParameter"
    }
)

create_instance(
    "LogisticsSensor",
    "LogisticsSensor_TruckGPS_02",
    {
        "sensorID": "S004",
        "sensorName": "Truck GPS Sensor 02",
        "sensorType": "GPSSensor",  # ✅ FIXED
        "status": "Active",
        "dataSourceType": "IoT",
        "conformsToStandard": "SSN/SOSA"
    },
    {
        "isDeployedOnShipment": "Shipment_Furniture_Truck01",
        "monitorsParameter": "RouteDeviationParameter"
    }
)


# -------------------------------------------------
# 5 EVENTS (TIMESTAMP FIXED)
# -------------------------------------------------

create_instance(
    "DeliveryDelayEvent",
    "DeliveryDelayEvent_RawMaterial",
    {
        "eventID": "E001",
        "eventTimestamp": now_timestamp()
    },
    {
        "affectsShipment": "Shipment_RawMaterial_001"
    }
)

create_instance(
    "PredictiveMaintenanceEvent",
    "PredictiveMaintenanceEvent_CNC01",
    {
        "eventID": "E002",
        "eventTimestamp": now_timestamp()
    },
    {
        "affectsMachine": "CNC_CuttingMachine_01"
    }
)

create_instance(
    "ProductionBottleneckEvent",
    "ProductionBottleneckEvent_Assembly",
    {
        "eventID": "E003",
        "eventTimestamp": now_timestamp()
    },
    {
        "affectsProcess": "FurnitureAssemblyProcess"
    }
)

create_instance(
    "TransportationDisruptionEvent",
    "TransportationDisruptionEvent_Truck01",
    {
        "eventID": "E004",
        "eventTimestamp": now_timestamp()
    },
    {
        "affectsShipment": "Shipment_Furniture_Truck01"
    }
)


# -------------------------------------------------
# 6 OBSERVATIONS (ALL REQUIRED FIELDS)
# -------------------------------------------------

def create_observation(name, value, unit, sensor, parameter, event):
    create_instance(
        "SensorObservation",
        name,
        {
            "observationID": name,
            "observationTimestamp": now_timestamp(),
            "observedValue": str(value),
            "unitOfMeasure": unit
        },
        {
            "observedBy": sensor,
            "observesParameter": parameter,
            "triggersEvent": event
        }
    )


create_observation(
    "Observation_Vibration_01",
    8.7,
    "mm/s",
    "VibrationSensor_CNC01",
    "MachineVibrationParameter",
    "PredictiveMaintenanceEvent_CNC01"
)

create_observation(
    "Observation_CycleTime_01",
    22,
    "seconds",
    "CycleTimeSensor_Assembly",
    "CycleTimeMetric",
    "ProductionBottleneckEvent_Assembly"
)

create_observation(
    "Observation_TransitDelay_01",
    14,
    "hours",
    "LogisticsSensor_TruckGPS_01",
    "TransitTimeParameter",
    "DeliveryDelayEvent_RawMaterial"
)

create_observation(
    "Observation_RouteDeviation_01",
    5,
    "km",
    "LogisticsSensor_TruckGPS_02",
    "RouteDeviationParameter",
    "TransportationDisruptionEvent_Truck01"
)


# -------------------------------------------------
# 7 STREAMING (SAFE)
# -------------------------------------------------

def generate_stream(sensor, parameter, event, prefix, base, unit):
    for i in range(5):
        value = round(base + random.uniform(-2, 2), 2)

        create_observation(
            f"{prefix}_{i}",
            value,
            unit,
            sensor,
            parameter,
            event
        )

        time.sleep(0.5)


generate_stream(
    "VibrationSensor_CNC01",
    "MachineVibrationParameter",
    "PredictiveMaintenanceEvent_CNC01",
    "Stream_Vibration",
    8.5,
    "mm/s"
)

generate_stream(
    "CycleTimeSensor_Assembly",
    "CycleTimeMetric",
    "ProductionBottleneckEvent_Assembly",
    "Stream_CycleTime",
    20,
    "seconds"
)

print("\n✅ ALL DATA CREATED — SHOULD NOW BE CONSISTENT\n")

print("\nStreaming simulation finished.\n")

"""
print("\n--- Creating Test Sensor ---")

create_instance(
    "ConditionMonitoringSensor",
    "VibrationSensor_CNC01",
    {},
    {
        # Optional but recommended if you have a machine:
        "isDeployedOnMachine": "MACH-001"
    }
)

print("\n--- TEST: Inverse Property Check ---")

create_instance(
    "SensorObservation",
    "TestObservation_001",
    {
        "observationID": "TEST-001",
        "observedValue": 10
    },
    {
        "observedBy": "VibrationSensor_CNC01"
    }
)
"""
