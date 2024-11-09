package org.lulunoel2016.unityMC.utils;

import org.lulunoel2016.unityMC.UnityMC;

public interface IModule {
    void onEnable(UnityMC plugin); // Méthode pour démarrer le module
    void onDisable(); // Méthode pour arrêter le module
    void setupDatabase(); // Méthode pour préparé la table
    boolean isEnabled(); // Boolean pour vérifié le statue d'un module
    void setEnabled(boolean enabled); // Méthode pour vérifié le statue d'un module
}
