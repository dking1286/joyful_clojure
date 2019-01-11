const path = require('path');

module.exports = {
  entry: './foreign_libs.js',
  output: {
    path: path.resolve(__dirname, 'target/public'),
    filename: 'foreign_libs.bundle.js',
  },
};
