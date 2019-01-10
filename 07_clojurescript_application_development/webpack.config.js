const path = require('path');

module.exports = {
  entry: './foreign_libs.js',
  output: {
    path: path.resolve(__dirname, 'resources/public/webpack-out'),
    filename: 'foreign_libs.js',
  },
};
