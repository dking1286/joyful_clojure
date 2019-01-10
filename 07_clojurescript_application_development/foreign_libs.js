import React from 'react';
import ReactDOM from 'react-dom';
import createReactClass from 'create-react-class';
import {
  AppBar,
  Button,
  Card,
  IconButton,
  Menu,
  MenuItem,
  MuiThemeProvider,
  Toolbar,
  Typography,
} from '@material-ui/core';
import {
  AccountCircle,
  Menu as MenuIcon,
} from '@material-ui/icons';

window.React = React;
window.ReactDOM = ReactDOM;
window.createReactClass = createReactClass;
window.MaterialUi = {
  AppBar,
  Button,
  Card,
  IconButton,
  Menu,
  MenuItem,
  MuiThemeProvider,
  Toolbar,
  Typography,
};
window.MaterialUiIcons = {
  AccountCircle,
  MenuIcon,
};
