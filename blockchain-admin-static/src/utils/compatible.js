if (!document.contains) {
    document.contains = function (search) {
        return document.body.contains(search)
    }
}