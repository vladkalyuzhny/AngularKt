const moduleRules = [{
    test: /\.html$/,
    use: [{
        loader: 'html-loader',
        options: {
            minimize: true
        }
    }]
}, {
    test: /\.css$/,
    use: [
        'style-loader',
        'css-loader'
    ]
}, {
    test: /\.(png|svg|jpe?g|gif)$/,
    use: [
        'file-loader'
    ]
}, {
    test: /\.(woff2?|eot|ttf|otf)$/,
    use: [
        'file-loader'
    ]
}];
config.module.rules.push(...moduleRules);