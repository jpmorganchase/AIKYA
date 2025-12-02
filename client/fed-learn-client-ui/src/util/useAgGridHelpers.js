import { useDensity, useTheme } from "@salt-ds/core";

import { LicenseManager } from "ag-grid-enterprise";

import "ag-grid-enterprise";
import {
 HTMLAttributes,
  useCallback,
  useMemo,
  useRef,
  useState,
} from "react";

LicenseManager.setLicenseKey("Using_this_AG_Grid_Enterprise_key_( AG-049143 )_in_excess_of_the_licence_granted_is_not_permitted___Please_report_misuse_to_( legal@ag-grid.com )___For_help_with_changing_this_key_please_contact_( info@ag-grid.com )___( JPMorgan Chase & Co. )_is_granted_a_( Multiple Applications )_Developer_License_for_( 2 )_Front-End_JavaScript_developers___All_Front-End_JavaScript_developers_need_to_be_licensed_in_addition_to_the_ones_working_with_AG_Grid_Enterprise___This_key_has_not_been_granted_a_Deployment_License_Add-on___This_key_works_with_AG_Grid_Enterprise_versions_released_before_( 18 December 2024 )____[v2]_MTczNDQ4MDAwMDAwMA==0ba829ec60d3d66bed6db9995f0ee3bb");


// Helps to set className, rowHeight and headerHeight depending on the current density
export function useAgGridHelpers() {
  const apiRef = useRef();
  const [isGridReady, setGridReady] = useState(false);


  const mode = useTheme();
  const density = useDensity();

  const [rowHeight, headerRowHeight] = useMemo(() => {
    switch (density) {
      case "high":
        return [25, 24]; // 20 + 4 + [1 (border)]
      case "medium":
        return [37, 36]; // 28 + 8 + [1 (border)]
      case "low":
        return [49, 48]; // 36 + 12 + [1 (border)]
      case "touch":
        return [61, 60]; // 44 + 16 + [1 (border)]
      default:
        return [25, 24];
    }
  }, [density]);


  const onGridReady = useCallback(({ api, columnApi }) => {
    apiRef.current = { api, columnApi };
    api.sizeColumnsToFit();
    setGridReady(true);
  }, []);

  return {
    containerProps: {
      style: { height: 500, width: '100% '},
    },
    agGridProps: {
      onGridReady,
      rowHeight,
      headerHeight: headerRowHeight,
      // suppressMenuHide: false,
      suppressContextMenu: false,
      defaultColDef: {
        // filter: true,
        resizable: true,
        sortable: true,
        // filterParams: {
        //   cellHeight: rowHeight,
        // },
      },
    },
    isGridReady,
    api: apiRef.current?.api,
    columnApi: apiRef.current?.columnApi,
  };
}