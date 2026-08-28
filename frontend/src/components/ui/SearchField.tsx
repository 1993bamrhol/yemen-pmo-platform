import type { InputProps } from "./Input";
import { Input } from "./Input";
import styles from "./Field.module.css";

export type SearchFieldProps = Omit<InputProps, "endAdornment" | "type">;

export function SearchField(props: SearchFieldProps) {
  return (
    <Input
      {...props}
      endAdornment={<span className={styles.searchIcon} />}
      type="search"
    />
  );
}
