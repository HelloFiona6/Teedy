$scope.translate = function() {
    console.log('Translating document with shareId:', $stateParams.share);
    Restangular.one('document', $scope.document.id)
        .all('translate/auto')
        .post({
            lang: $scope.targetLang,
            userId: $scope.document.userId,
            share: $stateParams.share
        })
        .then(function(response) {
// ... existing code ...
        });
}; 