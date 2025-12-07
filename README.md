# 📄 Documentation des Contraintes de l'API de Compilation C

Cette documentation détaille la structure JSON attendue et les règles strictes qui régissent la signature de la fonction et le code C soumis.

-----

## 1\. Structure du Corps de Requête JSON

Le corps de la requête HTTP doit être un objet JSON unique contenant les champs suivants :

| Champ | Type JSON | Description |
| :--- | :--- | :--- |
| **`signature`** | `Object` | Définit la signature de la fonction C (type de retour, nom, types des paramètres). |
| **`code`** | `String` | Contient la définition complète de la fonction C à tester. |
| **`returnType`** | `String` | Type de retour de la fonction, répété pour la clarté. |
| **`testCases`** | `Array` | Liste des tests à exécuter. |
| **`is_in_place`** | `Boolean` | Indique si la fonction modifie l'entrée (`true`) ou retourne une nouvelle allocation (`false`). |

-----

## 2\. Types C Autorisés

Les types suivants sont les seuls types C reconnus par le système.

| Catégorie | Types C Autorisés (JSON : `String`) |
| :--- | :--- |
| **Primitifs (Scalaires)** | `int`, `float`, `double`, `long`, `char`, **`bool`** |
| **Pointeurs de Primitifs (Tableaux)** | `int*`, `float*`, `double*`, `long*`, **`bool*`** |
| **Chaînes / Tableaux de Chaînes** | `char*` (Chaîne simple), `char**` (Tableau de chaînes) |

-----

## 3\. Contraintes de Codage C et de Signature

### 3.1. Contraintes de la Fonction

  * **`void` Interdit :** La fonction soumise doit toujours retourner une valeur. Le type de retour `void` n'est **pas autorisé**.
  * **`const` Interdit :** Le mot-clé **`const`** ne doit être utilisé nulle part dans la signature de la fonction.

### 3.2. Contrainte des Tableaux de Sortie

Pour toute fonction dont le type de retour est un tableau (`int*`, `bool*`, `char**`, etc.) :

  * **Pointeur de Taille :** Le **dernier paramètre** de la fonction doit obligatoirement être un pointeur (`int*`) qui reçoit la taille du tableau de sortie.

-----

## 4\. Contraintes de Formatage des `testCases` 📋

### 4.1. Représentation des Tableaux (`args` et `expected`)

Les tableaux doivent être représentés par une **chaîne de caractères** respectant le format d'initialisation des tableaux C, encadrée par des accolades `{}`.

| Type de Tableau | Format de Représentation | Exemple dans JSON (Argument ou Résultat) |
| :--- | :--- | :--- |
| **`int*`, `long*`** | Valeurs séparées par des virgules (sans espace). | `"{10, 31, 100}"` |
| **`float*`, `double*`** | Valeurs avec six décimales après la virgule (pour `expected`). | `"{0.500000, 1.000000}"` |
| **`bool*`** | Mots-clés `true` ou `false` (en minuscules). | `"{false, true, false}"` |
| **`char**` (Tableau de chaînes) | Chaque chaîne doit être encadrée par des guillemets doubles échappés. | `"{`**`\"Apple\"`**` ,  `**`\"Cherry\"`\*\*`}"` |
| **Tableau Vide** | Deux accolades sans contenu. | `"{}"` |

### 4.2. Représentation des Scalaires et Chaînes

| Type | Représentation JSON | Notes |
| :--- | :--- | :--- |
| **Chaîne** (`char*`) | Chaîne encadrée par des guillemets. | Ex: `"`**`\"hello world\"`**`"` |
| **`NULL`** | Littéral `"NULL"` (sans guillemets d'encadrement). | Pour les arguments de type pointeur. |
| **`\0`** | **`\\0`** | Le NUL ASCII doit être échappé dans la chaîne JSON (Ex: `"`**`\"test\\0\"`**`"`). |

### 4.3. Exemple de Test Case

```json
{
    "args": ["{1, 2, 3, 4, 5, 0}", "6", "0"],
    "expected": "{false, true, false, true, false, true}"
}
```

  * **Argument 1 (`int*`) :** `"{1, 2, 3, 4, 5, 0}"` (Tableau d'entiers)
  * **Argument 3 (`int*` de sortie) :** `"0"` (Initialisation)
  * **Résultat Attendu (`bool*`) :** `"{false, true, false, true, false, true}"` (Tableau de booléens)
