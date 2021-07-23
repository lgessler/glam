(ns glam.client.ui.material-ui
  "Wrappers for material-ui components

  Most code generated by navigating to material-ui.com/api/ and using some js on the component list:

  let classNames = Array.from($0.children).map(c => c.innerText.replace(/ /g, ''))
  let title2kebab = (s) => s.split('').map((c, i) => c === c.toUpperCase() && i > 0 ? '-' + c.toLowerCase() : c.toLowerCase()).join('')
  classNames.map(n => `[\"@material-ui/core/${n}\" :default ${n}]`).join('\\n')
  classNames.map(n => `(def ${title2kebab(n)} (interop/react-factory ${n}))`).join('\\n')
  "
  (:refer-clojure :exclude [list alert])
  (:require [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
            ["@material-ui/core/styles" :refer [styled createMuiTheme ThemeProvider]]
            ["@material-ui/core/colors/green" :default green]
            ["@material-ui/core/colors/purple" :default purple]
            ["@material-ui/core/colors/blue" :default blue]

    ;; BEGIN AUTO GEN
            ["@material-ui/core/Accordion" :default Accordion]
            ["@material-ui/core/AccordionActions" :default AccordionActions]
            ["@material-ui/core/AccordionDetails" :default AccordionDetails]
            ["@material-ui/core/AccordionSummary" :default AccordionSummary]
            ["@material-ui/core/AppBar" :default AppBar]
            ["@material-ui/core/Avatar" :default Avatar]
            ["@material-ui/core/Backdrop" :default Backdrop]
            ["@material-ui/core/Badge" :default Badge]
            ["@material-ui/core/BottomNavigation" :default BottomNavigation]
            ["@material-ui/core/BottomNavigationAction" :default BottomNavigationAction]
            ["@material-ui/core/Box" :default Box]
            ["@material-ui/core/Breadcrumbs" :default Breadcrumbs]
            ["@material-ui/core/Button" :default Button]
            ["@material-ui/core/ButtonBase" :default ButtonBase]
            ["@material-ui/core/ButtonGroup" :default ButtonGroup]
            ["@material-ui/core/Card" :default Card]
            ["@material-ui/core/CardActionArea" :default CardActionArea]
            ["@material-ui/core/CardActions" :default CardActions]
            ["@material-ui/core/CardContent" :default CardContent]
            ["@material-ui/core/CardHeader" :default CardHeader]
            ["@material-ui/core/CardMedia" :default CardMedia]
            ["@material-ui/core/Checkbox" :default Checkbox]
            ["@material-ui/core/Chip" :default Chip]
            ["@material-ui/core/CircularProgress" :default CircularProgress]
            ["@material-ui/core/ClickAwayListener" :default ClickAwayListener]
            ["@material-ui/core/Collapse" :default Collapse]
            ["@material-ui/core/Container" :default Container]
            ["@material-ui/core/CssBaseline" :default CssBaseline]
            ["@material-ui/core/Dialog" :default Dialog]
            ["@material-ui/core/DialogActions" :default DialogActions]
            ["@material-ui/core/DialogContent" :default DialogContent]
            ["@material-ui/core/DialogContentText" :default DialogContentText]
            ["@material-ui/core/DialogTitle" :default DialogTitle]
            ["@material-ui/core/Divider" :default Divider]
            ["@material-ui/core/Drawer" :default Drawer]
            ["@material-ui/core/ExpansionPanel" :default ExpansionPanel]
            ["@material-ui/core/ExpansionPanelActions" :default ExpansionPanelActions]
            ["@material-ui/core/ExpansionPanelDetails" :default ExpansionPanelDetails]
            ["@material-ui/core/ExpansionPanelSummary" :default ExpansionPanelSummary]
            ["@material-ui/core/Fab" :default Fab]
            ["@material-ui/core/Fade" :default Fade]
            ["@material-ui/core/FilledInput" :default FilledInput]
            ["@material-ui/core/FormControl" :default FormControl]
            ["@material-ui/core/FormControlLabel" :default FormControlLabel]
            ["@material-ui/core/FormGroup" :default FormGroup]
            ["@material-ui/core/FormHelperText" :default FormHelperText]
            ["@material-ui/core/FormLabel" :default FormLabel]
            ["@material-ui/core/Grid" :default Grid]
            ["@material-ui/core/GridList" :default GridList]
            ["@material-ui/core/GridListTile" :default GridListTile]
            ["@material-ui/core/GridListTileBar" :default GridListTileBar]
            ["@material-ui/core/Grow" :default Grow]
            ["@material-ui/core/Hidden" :default Hidden]
            ["@material-ui/core/Icon" :default Icon]
            ["@material-ui/core/IconButton" :default IconButton]
            ["@material-ui/core/Input" :default Input]
            ["@material-ui/core/InputAdornment" :default InputAdornment]
            ["@material-ui/core/InputBase" :default InputBase]
            ["@material-ui/core/InputLabel" :default InputLabel]
            ["@material-ui/core/LinearProgress" :default LinearProgress]
            ["@material-ui/core/Link" :default Link]
            ["@material-ui/core/List" :default List]
            ["@material-ui/core/ListItem" :default ListItem]
            ["@material-ui/core/ListItemAvatar" :default ListItemAvatar]
            ["@material-ui/core/ListItemIcon" :default ListItemIcon]
            ["@material-ui/core/ListItemSecondaryAction" :default ListItemSecondaryAction]
            ["@material-ui/core/ListItemText" :default ListItemText]
            ["@material-ui/core/ListSubheader" :default ListSubheader]
            ["@material-ui/core/Menu" :default Menu]
            ["@material-ui/core/MenuItem" :default MenuItem]
            ["@material-ui/core/MenuList" :default MenuList]
            ["@material-ui/core/MobileStepper" :default MobileStepper]
            ["@material-ui/core/Modal" :default Modal]
            ["@material-ui/core/NativeSelect" :default NativeSelect]
            ["@material-ui/core/NoSsr" :default NoSsr]
            ["@material-ui/core/OutlinedInput" :default OutlinedInput]
            ["@material-ui/core/Paper" :default Paper]
            ["@material-ui/core/Popover" :default Popover]
            ["@material-ui/core/Popper" :default Popper]
            ["@material-ui/core/Portal" :default Portal]
            ["@material-ui/core/Radio" :default Radio]
            ["@material-ui/core/RadioGroup" :default RadioGroup]
            ["@material-ui/core/RootRef" :default RootRef]
            ["@material-ui/core/ScopedCssBaseline" :default ScopedCssBaseline]
            ["@material-ui/core/Select" :default Select]
            ["@material-ui/core/Slide" :default Slide]
            ["@material-ui/core/Slider" :default Slider]
            ["@material-ui/core/Snackbar" :default Snackbar]
            ["@material-ui/core/SnackbarContent" :default SnackbarContent]
            ["@material-ui/core/Step" :default Step]
            ["@material-ui/core/StepButton" :default StepButton]
            ["@material-ui/core/StepConnector" :default StepConnector]
            ["@material-ui/core/StepContent" :default StepContent]
            ["@material-ui/core/StepIcon" :default StepIcon]
            ["@material-ui/core/StepLabel" :default StepLabel]
            ["@material-ui/core/Stepper" :default Stepper]
            ["@material-ui/core/SvgIcon" :default SvgIcon]
            ["@material-ui/core/SwipeableDrawer" :default SwipeableDrawer]
            ["@material-ui/core/Switch" :default Switch]
            ["@material-ui/core/Tab" :default Tab]
            ["@material-ui/core/Table" :default Table]
            ["@material-ui/core/TableBody" :default TableBody]
            ["@material-ui/core/TableCell" :default TableCell]
            ["@material-ui/core/TableContainer" :default TableContainer]
            ["@material-ui/core/TableFooter" :default TableFooter]
            ["@material-ui/core/TableHead" :default TableHead]
            ["@material-ui/core/TablePagination" :default TablePagination]
            ["@material-ui/core/TableRow" :default TableRow]
            ["@material-ui/core/TableSortLabel" :default TableSortLabel]
            ["@material-ui/core/Tabs" :default Tabs]
            ["@material-ui/core/TabScrollButton" :default TabScrollButton]
            ["@material-ui/core/TextareaAutosize" :default TextareaAutosize]
            ["@material-ui/core/TextField" :default TextField]
            ["@material-ui/core/Toolbar" :default Toolbar]
            ["@material-ui/core/Tooltip" :default Tooltip]
            ["@material-ui/core/Typography" :default Typography]
            ["@material-ui/core/Unstable_TrapFocus" :default Unstable_TrapFocus]
            ["@material-ui/core/Zoom" :default Zoom]
    ;; END AUTO GEN

    ;; had to manually move these down from above and change core to lab
            ["@material-ui/lab/Alert" :default Alert]
            ["@material-ui/lab/AlertTitle" :default AlertTitle]
            ["@material-ui/lab/Autocomplete" :default Autocomplete]
            ["@material-ui/lab/AvatarGroup" :default AvatarGroup]
            ["@material-ui/lab/Pagination" :default Pagination]
            ["@material-ui/lab/PaginationItem" :default PaginationItem]
            ["@material-ui/lab/Rating" :default Rating]
            ["@material-ui/lab/Skeleton" :default Skeleton]
            ["@material-ui/lab/SpeedDial" :default SpeedDial]
            ["@material-ui/lab/SpeedDialAction" :default SpeedDialAction]
            ["@material-ui/lab/SpeedDialIcon" :default SpeedDialIcon]
            ["@material-ui/lab/TabContext" :default TabContext]
            ["@material-ui/lab/TabList" :default TabList]
            ["@material-ui/lab/TabPanel" :default TabPanel]
            ["@material-ui/lab/Timeline" :default Timeline]
            ["@material-ui/lab/TimelineConnector" :default TimelineConnector]
            ["@material-ui/lab/TimelineContent" :default TimelineContent]
            ["@material-ui/lab/TimelineDot" :default TimelineDot]
            ["@material-ui/lab/TimelineItem" :default TimelineItem]
            ["@material-ui/lab/TimelineOppositeContent" :default TimelineOppositeContent]
            ["@material-ui/lab/TimelineSeparator" :default TimelineSeparator]
            ["@material-ui/lab/ToggleButton" :default ToggleButton]
            ["@material-ui/lab/ToggleButtonGroup" :default ToggleButtonGroup]
            ["@material-ui/lab/TreeItem" :default TreeItem]
            ["@material-ui/lab/TreeView" :default TreeView]
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
(def click-away-listener (interop/react-factory ClickAwayListener))
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
(def expansion-panel (interop/react-factory ExpansionPanel))
(def expansion-panel-actions (interop/react-factory ExpansionPanelActions))
(def expansion-panel-details (interop/react-factory ExpansionPanelDetails))
(def expansion-panel-summary (interop/react-factory ExpansionPanelSummary))
(def fab (interop/react-factory Fab))
(def fade (interop/react-factory Fade))
(def filled-input (interop/react-factory FilledInput))
(def form-control (interop/react-factory FormControl))
(def form-control-label (interop/react-factory FormControlLabel))
(def form-group (interop/react-factory FormGroup))
(def form-helper-text (interop/react-factory FormHelperText))
(def form-label (interop/react-factory FormLabel))
(def grid (interop/react-factory Grid))
(def grid-list (interop/react-factory GridList))
(def grid-list-tile (interop/react-factory GridListTile))
(def grid-list-tile-bar (interop/react-factory GridListTileBar))
(def grow (interop/react-factory Grow))
(def hidden (interop/react-factory Hidden))
(def icon (interop/react-factory Icon))
(def icon-button (interop/react-factory IconButton))
(def input (interop/react-factory Input))
(def input-adornment (interop/react-factory InputAdornment))
(def input-base (interop/react-factory InputBase))
(def input-label (interop/react-factory InputLabel))
(def linear-progress (interop/react-factory LinearProgress))
(def link (interop/react-factory Link))
(def list (interop/react-factory List))
(def list-item (interop/react-factory ListItem))
(def list-item-avatar (interop/react-factory ListItemAvatar))
(def list-item-icon (interop/react-factory ListItemIcon))
(def list-item-secondary-action (interop/react-factory ListItemSecondaryAction))
(def list-item-text (interop/react-factory ListItemText))
(def list-subheader (interop/react-factory ListSubheader))
(def menu (interop/react-factory Menu))
(def menu-item (interop/react-factory MenuItem))
(def menu-list (interop/react-factory MenuList))
(def mobile-stepper (interop/react-factory MobileStepper))
(def modal (interop/react-factory Modal))
(def native-select (interop/react-factory NativeSelect))
(def no-ssr (interop/react-factory NoSsr))
(def outlined-input (interop/react-factory OutlinedInput))
(def pagination (interop/react-factory Pagination))
(def pagination-item (interop/react-factory PaginationItem))
(def paper (interop/react-factory Paper))
(def popover (interop/react-factory Popover))
(def popper (interop/react-factory Popper))
(def portal (interop/react-factory Portal))
(def radio (interop/react-factory Radio))
(def radio-group (interop/react-factory RadioGroup))
(def rating (interop/react-factory Rating))
(def root-ref (interop/react-factory RootRef))
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
(def textarea-autosize (interop/react-factory TextareaAutosize))
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
(def tree-item (interop/react-factory TreeItem))
(def tree-view (interop/react-factory TreeView))
(def typography (interop/react-factory Typography))
(def unstable-trap-focus (interop/react-factory Unstable_TrapFocus))
(def zoom (interop/react-factory Zoom))
;; END AUTO GEN

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
