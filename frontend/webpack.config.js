const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const webpack = require('webpack');

module.exports = {
    entry: path.resolve(__dirname, 'src', 'index.js'),

    output: {
        path: path.resolve(__dirname, 'build'),
        filename: 'static/js/bundle.[contenthash].js',
        publicPath: '/',
        clean: true,
    },

    module: {
        rules: [
            {
                test: /\.jsx?$/,
                exclude: /node_modules/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: [
                            ['@babel/preset-env', { targets: 'defaults' }],
                            ['@babel/preset-react', { runtime: 'automatic' }],
                        ],
                    },
                },
            },
            {
                test: /\.css$/i,
                use: ['style-loader', 'css-loader'],
            },
        ],
    },

    resolve: {
        extensions: ['.js', '.jsx'],
        alias: {
            'process/browser': require.resolve('process/browser.js'),
        },
        fallback: {
            process: require.resolve('process/browser'), // ensures process exists in browser
        },
    },

    plugins: [
        new HtmlWebpackPlugin({
            template: path.resolve(__dirname, 'public', 'index.html'),
        }),

        // Provide global process object to browser
        new webpack.ProvidePlugin({
            process: 'process/browser',
        }),

        // Only define the variables you need
        new webpack.DefinePlugin({
            'process.env.REACT_APP_API_URL': JSON.stringify(
                process.env.REACT_APP_API_URL || 'http://localhost'
            ),
        }),
    ],

    devServer: {
        static: {
            directory: path.resolve(__dirname, 'public'),
        },
        historyApiFallback: true, // supports SPA routing
        port: 3000,
        host: '0.0.0.0',
        hot: true,
        open: true,
    },

    mode: process.env.NODE_ENV || 'development',
};
