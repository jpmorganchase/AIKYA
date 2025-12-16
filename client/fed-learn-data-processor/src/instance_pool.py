from typing import Any

_object_pool = dict()


def load_object(obj_name: str, obj: Any):
    """
    Loads a given instance to the global instance pool to mock DI
    Args:
        obj_name: unique object name
        obj: instance to be loaded

    Returns: True if object is loaded. false otherwise

    """
    if obj_name in _object_pool:
        return False

    _object_pool[obj_name] = obj
    return True


def get_object(obj_name: str) -> Any:
    """
    Gets a given instance from the global instance pool to mock DI
    Args:
        obj_name: name of the object to get. note that this object must be loaded previously and not removed
    Returns: instance
    """
    return _object_pool[obj_name] if obj_name in _object_pool else None


def remove_object(obj_name: str) -> Any:
    """
    Removes a given instance from the global instance pool to mock DI
    Args:
        obj_name: object instance to be removed from the pool.
        note that it does not delete the instance itself.

    Returns:
        Instance which was removed
    """
    return _object_pool.pop(obj_name, None)
