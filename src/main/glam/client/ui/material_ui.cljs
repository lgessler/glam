(ns glam.client.ui.material-ui
  "Wrappers for material-ui components

  Most code generated by navigating to https://mui.com/material-ui/migration/migration-v4/
  and using some js on the \"Component API\" HTML element (before running, select the <ul>
  element in the dev tools panel so that $0 will refer to it):

  let pascalNames = Array.from($0.childNodes).map(x => x.innerText)
  console.log(pascalNames.map(x => `[\"@mui/material/${x}\" :default ${x}]`).join(\"\\n\"))
  let pascal2kebab = (s) => s.split('').map((c, i) => c === c.toUpperCase() && i > 0 ? '-' + c.toLowerCase() : c.toLowerCase()).join('')\n\n
  console.log(pascalNames.map(n => `(def ${pascal2kebab(n)} (interop/react-factory ${n}))`).join('\\n'))
  "
  (:refer-clojure :exclude [list alert])
  (:require [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
            ["@mui/material/styles" :refer [styled createMuiTheme ThemeProvider]]
            ["@mui/material/colors/green" :default green]
            ["@mui/material/colors/purple" :default purple]
            ["@mui/material/colors/blue" :default blue]

    ;; BEGIN AUTO GEN
            ["@mui/material/Accordion" :default Accordion]
            ["@mui/material/AccordionActions" :default AccordionActions]
            ["@mui/material/AccordionDetails" :default AccordionDetails]
            ["@mui/material/AccordionSummary" :default AccordionSummary]
            ["@mui/material/Alert" :default Alert]
            ["@mui/material/AlertTitle" :default AlertTitle]
            ["@mui/material/AppBar" :default AppBar]
            ["@mui/material/Autocomplete" :default Autocomplete]
            ["@mui/material/Avatar" :default Avatar]
            ["@mui/material/AvatarGroup" :default AvatarGroup]
            ["@mui/material/Backdrop" :default Backdrop]
            ["@mui/material/Badge" :default Badge]
            ["@mui/material/BottomNavigation" :default BottomNavigation]
            ["@mui/material/BottomNavigationAction" :default BottomNavigationAction]
            ["@mui/material/Box" :default Box]
            ["@mui/material/Breadcrumbs" :default Breadcrumbs]
            ["@mui/material/Button" :default Button]
            ["@mui/material/ButtonBase" :default ButtonBase]
            ["@mui/material/ButtonGroup" :default ButtonGroup]
            ["@mui/material/Card" :default Card]
            ["@mui/material/CardActionArea" :default CardActionArea]
            ["@mui/material/CardActions" :default CardActions]
            ["@mui/material/CardContent" :default CardContent]
            ["@mui/material/CardHeader" :default CardHeader]
            ["@mui/material/CardMedia" :default CardMedia]
            ["@mui/material/Checkbox" :default Checkbox]
            ["@mui/material/Chip" :default Chip]
            ["@mui/material/CircularProgress" :default CircularProgress]
            ["@mui/material/Collapse" :default Collapse]
            ["@mui/material/Container" :default Container]
            ["@mui/material/CssBaseline" :default CssBaseline]
            ["@mui/material/Dialog" :default Dialog]
            ["@mui/material/DialogActions" :default DialogActions]
            ["@mui/material/DialogContent" :default DialogContent]
            ["@mui/material/DialogContentText" :default DialogContentText]
            ["@mui/material/DialogTitle" :default DialogTitle]
            ["@mui/material/Divider" :default Divider]
            ["@mui/material/Drawer" :default Drawer]
            ["@mui/material/Fab" :default Fab]
            ["@mui/material/Fade" :default Fade]
            ["@mui/material/FilledInput" :default FilledInput]
            ["@mui/material/FormControl" :default FormControl]
            ["@mui/material/FormControlLabel" :default FormControlLabel]
            ["@mui/material/FormGroup" :default FormGroup]
            ["@mui/material/FormHelperText" :default FormHelperText]
            ["@mui/material/FormLabel" :default FormLabel]
            ["@mui/material/GlobalStyles" :default GlobalStyles]
            ["@mui/material/Grid" :default Grid]
            ["@mui/material/Grow" :default Grow]
            ["@mui/material/Hidden" :default Hidden]
            ["@mui/material/Icon" :default Icon]
            ["@mui/material/IconButton" :default IconButton]
            ["@mui/material/ImageList" :default ImageList]
            ["@mui/material/ImageListItem" :default ImageListItem]
            ["@mui/material/ImageListItemBar" :default ImageListItemBar]
            ["@mui/material/Input" :default Input]
            ["@mui/material/InputAdornment" :default InputAdornment]
            ["@mui/material/InputBase" :default InputBase]
            ["@mui/material/InputLabel" :default InputLabel]
            ["@mui/material/LinearProgress" :default LinearProgress]
            ["@mui/material/Link" :default Link]
            ["@mui/material/List" :default List]
            ["@mui/material/ListItem" :default ListItem]
            ["@mui/material/ListItemAvatar" :default ListItemAvatar]
            ["@mui/material/ListItemButton" :default ListItemButton]
            ["@mui/material/ListItemIcon" :default ListItemIcon]
            ["@mui/material/ListItemSecondaryAction" :default ListItemSecondaryAction]
            ["@mui/material/ListItemText" :default ListItemText]
            ["@mui/material/ListSubheader" :default ListSubheader]
            ["@mui/lab/LoadingButton" :default LoadingButton]
            ["@mui/material/Menu" :default Menu]
            ["@mui/material/MenuItem" :default MenuItem]
            ["@mui/material/MenuList" :default MenuList]
            ["@mui/material/MobileStepper" :default MobileStepper]
            ["@mui/material/Modal" :default Modal]
            ["@mui/material/NativeSelect" :default NativeSelect]
            ["@mui/material/OutlinedInput" :default OutlinedInput]
            ["@mui/material/Pagination" :default Pagination]
            ["@mui/material/PaginationItem" :default PaginationItem]
            ["@mui/material/Paper" :default Paper]
            ["@mui/material/Popover" :default Popover]
            ["@mui/material/Popper" :default Popper]
            ["@mui/material/Radio" :default Radio]
            ["@mui/material/RadioGroup" :default RadioGroup]
            ["@mui/material/Rating" :default Rating]
            ["@mui/material/ScopedCssBaseline" :default ScopedCssBaseline]
            ["@mui/material/Select" :default Select]
            ["@mui/material/Skeleton" :default Skeleton]
            ["@mui/material/Slide" :default Slide]
            ["@mui/material/Slider" :default Slider]
            ["@mui/material/Snackbar" :default Snackbar]
            ["@mui/material/SnackbarContent" :default SnackbarContent]
            ["@mui/material/SpeedDial" :default SpeedDial]
            ["@mui/material/SpeedDialAction" :default SpeedDialAction]
            ["@mui/material/SpeedDialIcon" :default SpeedDialIcon]
            ["@mui/material/Stack" :default Stack]
            ["@mui/material/Step" :default Step]
            ["@mui/material/StepButton" :default StepButton]
            ["@mui/material/StepConnector" :default StepConnector]
            ["@mui/material/StepContent" :default StepContent]
            ["@mui/material/StepIcon" :default StepIcon]
            ["@mui/material/StepLabel" :default StepLabel]
            ["@mui/material/Stepper" :default Stepper]
            ["@mui/material/SvgIcon" :default SvgIcon]
            ["@mui/material/SwipeableDrawer" :default SwipeableDrawer]
            ["@mui/material/Switch" :default Switch]
            ["@mui/material/Tab" :default Tab]
            ["@mui/lab/TabContext" :default TabContext]
            ["@mui/material/Table" :default Table]
            ["@mui/material/TableBody" :default TableBody]
            ["@mui/material/TableCell" :default TableCell]
            ["@mui/material/TableContainer" :default TableContainer]
            ["@mui/material/TableFooter" :default TableFooter]
            ["@mui/material/TableHead" :default TableHead]
            ["@mui/material/TablePagination" :default TablePagination]
            ["@mui/material/TableRow" :default TableRow]
            ["@mui/material/TableSortLabel" :default TableSortLabel]
            ["@mui/lab/TabList" :default TabList]
            ["@mui/lab/TabPanel" :default TabPanel]
            ["@mui/material/Tabs" :default Tabs]
            ["@mui/material/TabScrollButton" :default TabScrollButton]
            ["@mui/material/TextField" :default TextField]
            ["@mui/lab/Timeline" :default Timeline]
            ["@mui/lab/TimelineConnector" :default TimelineConnector]
            ["@mui/lab/TimelineContent" :default TimelineContent]
            ["@mui/lab/TimelineDot" :default TimelineDot]
            ["@mui/lab/TimelineItem" :default TimelineItem]
            ["@mui/lab/TimelineOppositeContent" :default TimelineOppositeContent]
            ["@mui/lab/TimelineSeparator" :default TimelineSeparator]
            ["@mui/material/ToggleButton" :default ToggleButton]
            ["@mui/material/ToggleButtonGroup" :default ToggleButtonGroup]
            ["@mui/material/Toolbar" :default Toolbar]
            ["@mui/material/Tooltip" :default Tooltip]
            ["@mui/material/Typography" :default Typography]
            ["@mui/material/Zoom" :default Zoom]
    ;; END AUTO GEN
            ["@mui/x-tree-view" :default TreeView]
            ["@mui/x-tree-view/TreeItem" :default TreeItem]
            [glam.client.ui.material-ui-icon :as muic]
            [taoensso.timbre :as log]))


;; BEGIN AUTO GEN
(def accordion (interop/react-factory Accordion))
(def accordion-actions (interop/react-factory AccordionActions))
(def accordion-details (interop/react-factory AccordionDetails))
(def accordion-summary (interop/react-factory AccordionSummary))
(def alert (interop/react-factory Alert))
(def alert-title (interop/react-factory AlertTitle))
(def app-bar (interop/react-factory AppBar))
(def autocomplete (interop/react-factory Autocomplete))
(def avatar (interop/react-factory Avatar))
(def avatar-group (interop/react-factory AvatarGroup))
(def backdrop (interop/react-factory Backdrop))
(def badge (interop/react-factory Badge))
(def bottom-navigation (interop/react-factory BottomNavigation))
(def bottom-navigation-action (interop/react-factory BottomNavigationAction))
(def box (interop/react-factory Box))
(def breadcrumbs (interop/react-factory Breadcrumbs))
(def button (interop/react-factory Button))
(def button-base (interop/react-factory ButtonBase))
(def button-group (interop/react-factory ButtonGroup))
(def card (interop/react-factory Card))
(def card-action-area (interop/react-factory CardActionArea))
(def card-actions (interop/react-factory CardActions))
(def card-content (interop/react-factory CardContent))
(def card-header (interop/react-factory CardHeader))
(def card-media (interop/react-factory CardMedia))
(def checkbox (interop/react-factory Checkbox))
(def chip (interop/react-factory Chip))
(def circular-progress (interop/react-factory CircularProgress))
(def collapse (interop/react-factory Collapse))
(def container (interop/react-factory Container))
(def css-baseline (interop/react-factory CssBaseline))
(def dialog (interop/react-factory Dialog))
(def dialog-actions (interop/react-factory DialogActions))
(def dialog-content (interop/react-factory DialogContent))
(def dialog-content-text (interop/react-factory DialogContentText))
(def dialog-title (interop/react-factory DialogTitle))
(def divider (interop/react-factory Divider))
(def drawer (interop/react-factory Drawer))
(def fab (interop/react-factory Fab))
(def fade (interop/react-factory Fade))
(def filled-input (interop/react-factory FilledInput))
(def form-control (interop/react-factory FormControl))
(def form-control-label (interop/react-factory FormControlLabel))
(def form-group (interop/react-factory FormGroup))
(def form-helper-text (interop/react-factory FormHelperText))
(def form-label (interop/react-factory FormLabel))
(def global-styles (interop/react-factory GlobalStyles))
(def grid (interop/react-factory Grid))
(def grow (interop/react-factory Grow))
(def hidden (interop/react-factory Hidden))
(def icon (interop/react-factory Icon))
(def icon-button (interop/react-factory IconButton))
(def image-list (interop/react-factory ImageList))
(def image-list-item (interop/react-factory ImageListItem))
(def image-list-item-bar (interop/react-factory ImageListItemBar))
(def input (interop/react-factory Input))
(def input-adornment (interop/react-factory InputAdornment))
(def input-base (interop/react-factory InputBase))
(def input-label (interop/react-factory InputLabel))
(def linear-progress (interop/react-factory LinearProgress))
(def link (interop/react-factory Link))
(def list (interop/react-factory List))
(def list-item (interop/react-factory ListItem))
(def list-item-avatar (interop/react-factory ListItemAvatar))
(def list-item-button (interop/react-factory ListItemButton))
(def list-item-icon (interop/react-factory ListItemIcon))
(def list-item-secondary-action (interop/react-factory ListItemSecondaryAction))
(def list-item-text (interop/react-factory ListItemText))
(def list-subheader (interop/react-factory ListSubheader))
(def loading-button (interop/react-factory LoadingButton))
(def menu (interop/react-factory Menu))
(def menu-item (interop/react-factory MenuItem))
(def menu-list (interop/react-factory MenuList))
(def mobile-stepper (interop/react-factory MobileStepper))
(def modal (interop/react-factory Modal))
(def native-select (interop/react-factory NativeSelect))
(def outlined-input (interop/react-factory OutlinedInput))
(def pagination (interop/react-factory Pagination))
(def pagination-item (interop/react-factory PaginationItem))
(def paper (interop/react-factory Paper))
(def popover (interop/react-factory Popover))
(def popper (interop/react-factory Popper))
(def radio (interop/react-factory Radio))
(def radio-group (interop/react-factory RadioGroup))
(def rating (interop/react-factory Rating))
(def scoped-css-baseline (interop/react-factory ScopedCssBaseline))
(def select (interop/react-factory Select))
(def skeleton (interop/react-factory Skeleton))
(def slide (interop/react-factory Slide))
(def slider (interop/react-factory Slider))
(def snackbar (interop/react-factory Snackbar))
(def snackbar-content (interop/react-factory SnackbarContent))
(def speed-dial (interop/react-factory SpeedDial))
(def speed-dial-action (interop/react-factory SpeedDialAction))
(def speed-dial-icon (interop/react-factory SpeedDialIcon))
(def stack (interop/react-factory Stack))
(def step (interop/react-factory Step))
(def step-button (interop/react-factory StepButton))
(def step-connector (interop/react-factory StepConnector))
(def step-content (interop/react-factory StepContent))
(def step-icon (interop/react-factory StepIcon))
(def step-label (interop/react-factory StepLabel))
(def stepper (interop/react-factory Stepper))
(def svg-icon (interop/react-factory SvgIcon))
(def swipeable-drawer (interop/react-factory SwipeableDrawer))
(def switch (interop/react-factory Switch))
(def tab (interop/react-factory Tab))
(def tab-context (interop/react-factory TabContext))
(def table (interop/react-factory Table))
(def table-body (interop/react-factory TableBody))
(def table-cell (interop/react-factory TableCell))
(def table-container (interop/react-factory TableContainer))
(def table-footer (interop/react-factory TableFooter))
(def table-head (interop/react-factory TableHead))
(def table-pagination (interop/react-factory TablePagination))
(def table-row (interop/react-factory TableRow))
(def table-sort-label (interop/react-factory TableSortLabel))
(def tab-list (interop/react-factory TabList))
(def tab-panel (interop/react-factory TabPanel))
(def tabs (interop/react-factory Tabs))
(def tab-scroll-button (interop/react-factory TabScrollButton))
(def text-field (interop/react-factory TextField))
(def timeline (interop/react-factory Timeline))
(def timeline-connector (interop/react-factory TimelineConnector))
(def timeline-content (interop/react-factory TimelineContent))
(def timeline-dot (interop/react-factory TimelineDot))
(def timeline-item (interop/react-factory TimelineItem))
(def timeline-opposite-content (interop/react-factory TimelineOppositeContent))
(def timeline-separator (interop/react-factory TimelineSeparator))
(def toggle-button (interop/react-factory ToggleButton))
(def toggle-button-group (interop/react-factory ToggleButtonGroup))
(def toolbar (interop/react-factory Toolbar))
(def tooltip (interop/react-factory Tooltip))
(def typography (interop/react-factory Typography))
(def zoom (interop/react-factory Zoom))
;; END AUTO GEN
(def tree-view (interop/react-factory TreeView))
(def tree-item (interop/react-factory TreeItem))

;; styled variants
(defn- wrap-styles
  [component styles]
  (interop/react-factory
    ((styled component) (clj->js styles))))
(defn styled-button [styles] (wrap-styles Button styles))
(defn styled-container [styles] (wrap-styles Container styles))
(defn styled-fab [styles] (wrap-styles Fab styles))
(defn styled-box [styles] (wrap-styles Box styles))
(defn styled-icon [styles] (wrap-styles Icon styles))
(defn styled-list [styles] (wrap-styles List styles))
(defn styled-paper [styles] (wrap-styles Paper styles))
(defn styled-typography [styles] (wrap-styles Typography styles))
(defn styled-breadcrumbs [styles] (wrap-styles Breadcrumbs styles))
(defn styled-card-media [styles] (wrap-styles CardMedia styles))
(defn styled-select [styles] (wrap-styles Select styles))

;; theme
(def default-theme (createMuiTheme
                     #js {:spacing   8
                          :palette   #js {:primary   purple
                                          :secondary blue}
                          :overrides #js {:MuiTreeItem
                                          #js {:iconContainer #js {:width  "18px"
                                                                   "& svg" #js {:font-size "22px"}}
                                               :label         #js {:line-height "2.0"
                                                                   :font-size   "18px"}}}}))
(def theme-provider (interop/react-factory ThemeProvider))

;; --------------------------------------------------------------------------------
;; conveniences
;; --------------------------------------------------------------------------------
;; these are glam-specific functions that have been centralized here for consistency
(defn page-container
  ([attrs child]
   (container attrs
     (box {:mx 1 :my 2 :px 1 :py 2}
       child)))
  ([child]
   (page-container {} child)))

(defn padded-paper
  ([attrs child]
   (paper attrs
     (box {:m 1 :p 1}
       child)))
  ([child]
   (padded-paper {} child)))

(def minw-100-select (styled-select {:min-width "100px"}))

(def mt-typography (styled-typography {:margin-top "0.7em"}))
(defn page-title
  ([attrs child]
   (mt-typography (merge {:variant "h4"} attrs) child))
  ([child]
   (page-title {} child)))

(def mb-breadcrumbs (styled-breadcrumbs {:margin-bottom "1.8em"}))
(defn arrow-breadcrumbs [attrs & children]
  (mb-breadcrumbs (merge {:separator (muic/navigate-next)} attrs)
    children))

(defn vertical-grid [& [maybe-attr-map & _ :as children]]
  "Only use with static seqs! (key function is just the position in the list)"
  (let [have-map? (map? maybe-attr-map)
        attrs (merge {:container true :direction "column" :spacing 1} (if have-map? maybe-attr-map {}))
        children (if have-map? (rest children) children)]
    (grid attrs
      (map-indexed (fn [i child]
                     (grid {:item true
                            :key  i}
                       child))
                   children))))

(defn horizontal-grid [& [maybe-attr-map & _ :as children]]
  "Only use with static seqs! (key function is just the position in the list)"
  (let [have-map? (map? maybe-attr-map)
        attrs (merge {:container true :direction "row" :spacing 1} (if have-map? maybe-attr-map {}))
        children (if have-map? (rest children) children)]
    (grid attrs
      (map-indexed (fn [i child]
                     (grid {:item true
                            :key  i}
                       child))
                   children))))

(defn zero-state [message]
  (typography {:variant   "subtitle1"
               :component "h3"
               :color     "textSecondary"} message))
