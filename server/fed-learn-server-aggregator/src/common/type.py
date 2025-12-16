from dataclasses import dataclass
from enum import Enum
from typing import Any, Dict, List, Union

import numpy as np
import numpy.typing as npt

NDArray = npt.NDArray[Any]
NDArrayInt = npt.NDArray[np.int_]
NDArrayFloat = npt.NDArray[np.float64]
NDArrays = List[NDArray]

Scalar = Union[bool, bytes, float, int, str]
Value = Union[
    bool,
    bytes,
    float,
    int,
    str,
    List[bool],
    List[bytes],
    List[float],
    List[int],
    List[str],
]

class Code(Enum):
    """Client status codes."""
    OK = 0
    GET_PROPERTIES_NOT_IMPLEMENTED = 1
    GET_PARAMETERS_NOT_IMPLEMENTED = 2
    FIT_NOT_IMPLEMENTED = 3
    EVALUATE_NOT_IMPLEMENTED = 4


@dataclass
class Status:
    """Client status."""
    code: Code
    message: str

@dataclass
class Parameters:
    """Model parameters."""

    tensors: List[bytes]
    tensor_type: str


@dataclass
class FitRequest:
    """Fit instructions for a client."""

    parameters: Parameters
    config: Dict[str, Scalar]

@dataclass
class FitResponse:
    """Fit response from a client."""

    status: Status
    parameters: Parameters
    num_examples: int
    metrics: Dict[str, Scalar]

